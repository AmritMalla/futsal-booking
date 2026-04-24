import apiClient from './api';
import { Review, ReviewRequest } from '../types';

export const reviewService = {
  async createReview(data: ReviewRequest): Promise<Review> {
    const response = await apiClient.post<Review>('/reviews', data);
    return response.data;
  },

  async getReviewById(id: string): Promise<Review> {
    const response = await apiClient.get<Review>(`/reviews/${id}`);
    return response.data;
  },

  async getReviewsByGround(groundId: string): Promise<Review[]> {
    const response = await apiClient.get<Review[]>(`/reviews/ground/${groundId}`);
    return response.data;
  },

  async getReviewsByUser(userId: string): Promise<Review[]> {
    const response = await apiClient.get<Review[]>(`/reviews/user/${userId}`);
    return response.data;
  },

  async getAverageRating(groundId: string): Promise<number> {
    const response = await apiClient.get<number>(`/reviews/ground/${groundId}/rating`);
    return response.data;
  },

  async updateReview(id: string, data: ReviewRequest): Promise<Review> {
    const response = await apiClient.put<Review>(`/reviews/${id}`, data);
    return response.data;
  },

  async deleteReview(id: string): Promise<void> {
    await apiClient.delete(`/reviews/${id}`);
  },
};
