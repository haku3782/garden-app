import api from './axios'

export const getPlants = () =>
  api.get('/api/plants')

export const getPlant = (id) =>
  api.get(`/api/plants/${id}`)

export const createPlant = (data) =>
  api.post('/api/plants', data)

export const updatePlant = (id, data) =>
  api.put(`/api/plants/${id}`, data)

export const deletePlant = (id) =>
  api.delete(`/api/plants/${id}`)
