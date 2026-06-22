import api from './axios'

export const getCareLogs = (plantId) =>
  api.get(`/api/plants/${plantId}/care-logs`)

export const createCareLog = (plantId, data) =>
  api.post(`/api/plants/${plantId}/care-logs`, data)

export const deleteCareLog = (plantId, id) =>
  api.delete(`/api/plants/${plantId}/care-logs/${id}`)

export const uploadCareLogPhoto = (plantId, id, file) => {
  const formData = new FormData()
  formData.append('photo', file)
  return api.post(`/api/plants/${plantId}/care-logs/${id}/photo`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
