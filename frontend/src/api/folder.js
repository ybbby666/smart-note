import request from '@/utils/request'

/**
 * 创建文件夹
 */
export function createFolder(data) {
  return request.post('/folders', data)
}

/**
 * 重命名文件夹
 */
export function renameFolder(folderId, data) {
  return request.put(`/folders/${folderId}`, data)
}

/**
 * 删除文件夹
 */
export function deleteFolder(folderId) {
  return request.delete(`/folders/${folderId}`)
}

/**
 * 获取文件夹树
 */
export function getFolderTree() {
  return request.get('/folders/tree')
}
