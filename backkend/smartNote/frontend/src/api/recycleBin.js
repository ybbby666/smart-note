import request from '@/utils/request'

/**
 * 获取回收站列表
 */
export function getRecycleBinList() {
  return request.get('/recycle-bin')
}

/**
 * 从回收站恢复
 */
export function restoreFromRecycleBin(recycleBinId) {
  return request.put(`/recycle-bin/${recycleBinId}/restore`)
}

/**
 * 彻底删除
 */
export function permanentDelete(recycleBinId) {
  return request.delete(`/recycle-bin/${recycleBinId}`)
}

/**
 * 清空回收站
 */
export function clearRecycleBin() {
  return request.delete('/recycle-bin/clear')
}
