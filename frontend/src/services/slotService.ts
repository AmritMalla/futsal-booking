import apiClient from './api';
import { TimeSlot, TimeSlotRequest } from '../types';

export const slotService = {
  async createTimeSlot(data: TimeSlotRequest): Promise<TimeSlot> {
    const response = await apiClient.post<TimeSlot>('/slots', data);
    return response.data;
  },

  async getTimeSlotById(id: string): Promise<TimeSlot> {
    const response = await apiClient.get<TimeSlot>(`/slots/${id}`);
    return response.data;
  },

  async getTimeSlotsByGround(groundId: string): Promise<TimeSlot[]> {
    const response = await apiClient.get<TimeSlot[]>(`/slots/ground/${groundId}`);
    return response.data;
  },

  async getAvailableSlots(groundId: string): Promise<TimeSlot[]> {
    const response = await apiClient.get<TimeSlot[]>(`/slots/available/ground/${groundId}`);
    return response.data;
  },

  async getSlotsByDateRange(groundId: string, start: string, end: string): Promise<TimeSlot[]> {
    const response = await apiClient.get<TimeSlot[]>('/slots/date-range', {
      params: { groundId, start, end },
    });
    return response.data;
  },

  async markSlotAsBooked(slotId: string): Promise<TimeSlot> {
    const response = await apiClient.put<TimeSlot>(`/slots/${slotId}/book`);
    return response.data;
  },

  async updateTimeSlot(id: string, data: TimeSlot): Promise<TimeSlot> {
    const response = await apiClient.put<TimeSlot>(`/slots/${id}`, data);
    return response.data;
  },

  async deleteTimeSlot(id: string): Promise<void> {
    await apiClient.delete(`/slots/${id}`);
  },

  // Helper to generate slots for a day
  generateDailySlots(groundId: string, date: Date, startHour: number = 6, endHour: number = 22): TimeSlotRequest[] {
    const slots: TimeSlotRequest[] = [];

    for (let hour = startHour; hour < endHour; hour++) {
      const startTime = new Date(date);
      startTime.setHours(hour, 0, 0, 0);

      const endTime = new Date(date);
      endTime.setHours(hour + 1, 0, 0, 0);

      slots.push({
        groundId,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
      });
    }

    return slots;
  },

  // Bulk create slots for a day
  async createDailySlots(groundId: string, date: Date, startHour: number = 6, endHour: number = 22): Promise<TimeSlot[]> {
    const slotsToCreate = this.generateDailySlots(groundId, date, startHour, endHour);
    const createdSlots: TimeSlot[] = [];

    for (const slot of slotsToCreate) {
      try {
        const created = await this.createTimeSlot(slot);
        createdSlots.push(created);
      } catch (error) {
        console.error('Failed to create slot:', error);
      }
    }

    return createdSlots;
  },
};
