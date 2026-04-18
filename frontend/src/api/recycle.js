                          import request from '@/utils/request'

/**
 * 获取回收站列表
 */
export function getRecycleBinList() {
  return request.get('/recycle-bin')
}

/**
 * 恢复笔记/文件夹
 */
export function restoreFromRecycleBin(recycleBinId) {
  console.log('API调用 - restoreFromRecycleBin, ID:', recycleBinId, '类型:', typeof recycleBinId)
  if (!recycleBinId || recycleBinId === 'null' || recycleBinId === 'undefined') {
    console.error('无效的回收站ID:', recycleBinId)
    return Promise.reject(new Error('无效的回收站ID'))
  }
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
