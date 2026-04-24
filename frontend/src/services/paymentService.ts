import apiClient from './api';
import { Payment, PaymentRequest, PaymentStatus } from '../types';

// Stub payment processor - simulates payment processing
// Replace this with actual payment gateway integration (Stripe, PayPal, etc.)
const STUB_PAYMENT_DELAY = 2000; // 2 seconds delay to simulate processing

export const paymentService = {
  async createPayment(data: PaymentRequest): Promise<Payment> {
    const response = await apiClient.post<Payment>('/payments', data);
    return response.data;
  },

  async getPaymentById(id: string): Promise<Payment> {
    const response = await apiClient.get<Payment>(`/payments/${id}`);
    return response.data;
  },

  async getPaymentByTransaction(transactionId: string): Promise<Payment> {
    const response = await apiClient.get<Payment>(`/payments/transaction/${transactionId}`);
    return response.data;
  },

  async getPaymentsByBooking(bookingId: string): Promise<Payment[]> {
    const response = await apiClient.get<Payment[]>(`/payments/booking/${bookingId}`);
    return response.data;
  },

  async getPaymentsByUser(userId: string): Promise<Payment[]> {
    const response = await apiClient.get<Payment[]>(`/payments/user/${userId}`);
    return response.data;
  },

  async getPaymentsByStatus(status: PaymentStatus): Promise<Payment[]> {
    const response = await apiClient.get<Payment[]>(`/payments/status/${status}`);
    return response.data;
  },

  async updatePaymentStatus(id: string, status: PaymentStatus): Promise<Payment> {
    const response = await apiClient.put<Payment>(`/payments/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  async refundPayment(id: string): Promise<Payment> {
    const response = await apiClient.post<Payment>(`/payments/${id}/refund`);
    return response.data;
  },

  // ============================================
  // STUB PAYMENT METHODS (for development/testing)
  // Replace with actual payment gateway integration
  // ============================================

  /**
   * Simulates processing a payment
   * In production, this would integrate with Stripe, PayPal, etc.
   */
  async processStubPayment(data: {
    bookingId: string;
    amount: number;
    paymentMethod?: string;
  }): Promise<{
    success: boolean;
    transactionId: string;
    message: string;
  }> {
    // Simulate payment processing delay
    await new Promise((resolve) => setTimeout(resolve, STUB_PAYMENT_DELAY));

    // Generate a mock transaction ID
    const transactionId = `TXN-${Date.now()}-${Math.random().toString(36).substring(2, 8).toUpperCase()}`;

    // 95% success rate for testing (5% simulated failures)
    const success = Math.random() > 0.05;

    if (success) {
      // Create the actual payment record
      try {
        const paymentRequest: PaymentRequest = {
          bookingId: data.bookingId,
          amount: data.amount,
          transactionId,
        };
        await this.createPayment(paymentRequest);

        return {
          success: true,
          transactionId,
          message: 'Payment processed successfully',
        };
      } catch (error) {
        return {
          success: false,
          transactionId: '',
          message: 'Failed to record payment. Please try again.',
        };
      }
    } else {
      return {
        success: false,
        transactionId: '',
        message: 'Payment declined. Please check your payment details and try again.',
      };
    }
  },

  /**
   * Validates payment details (stub)
   * In production, this would validate with the payment gateway
   */
  validatePaymentDetails(details: {
    cardNumber?: string;
    expiryDate?: string;
    cvv?: string;
    paymentMethod: string;
  }): { valid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (details.paymentMethod === 'card') {
      // Basic validation for demo purposes
      if (!details.cardNumber || details.cardNumber.replace(/\s/g, '').length !== 16) {
        errors.push('Invalid card number');
      }
      if (!details.expiryDate || !/^\d{2}\/\d{2}$/.test(details.expiryDate)) {
        errors.push('Invalid expiry date (MM/YY)');
      }
      if (!details.cvv || !/^\d{3,4}$/.test(details.cvv)) {
        errors.push('Invalid CVV');
      }
    }

    return {
      valid: errors.length === 0,
      errors,
    };
  },

  /**
   * Gets available payment methods
   * In production, this might fetch from the payment gateway
   */
  getAvailablePaymentMethods(): { id: string; name: string; icon: string; enabled: boolean }[] {
    return [
      {
        id: 'cash',
        name: 'Cash on Arrival',
        icon: 'payments',
        enabled: true,
      },
      {
        id: 'esewa',
        name: 'eSewa',
        icon: 'account_balance_wallet',
        enabled: true,
      },
      {
        id: 'khalti',
        name: 'Khalti',
        icon: 'account_balance_wallet',
        enabled: true,
      },
      {
        id: 'card',
        name: 'Credit/Debit Card',
        icon: 'credit_card',
        enabled: false, // Disabled until actual integration
      },
    ];
  },

  /**
   * Simulates payment refund processing
   */
  async processStubRefund(paymentId: string): Promise<{
    success: boolean;
    message: string;
  }> {
    // Simulate refund processing delay
    await new Promise((resolve) => setTimeout(resolve, STUB_PAYMENT_DELAY));

    try {
      await this.refundPayment(paymentId);
      return {
        success: true,
        message: 'Refund processed successfully. Amount will be credited within 5-7 business days.',
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to process refund. Please contact support.',
      };
    }
  },
};
