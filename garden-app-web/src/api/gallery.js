import api from './axios'

export const getPhotoGallery = () =>
  api.get('/api/gallery/photos')
