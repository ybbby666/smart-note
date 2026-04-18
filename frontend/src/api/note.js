import request from '@/utils/request'

/**
 * 创建笔记
 */
export function createNote(data) {
  return request.post('/notes', data)
}

/**
 * 分页查询笔记列表
 */
export function getNoteList(params) {
  return request.get('/notes', { params })
}

/**
 * 获取笔记详情
 */
export function getNoteDetail(id) {
  return request.get(`/notes/${id}`)
}

/**
 * 更新笔记
 */
export function updateNote(id, data) {
  return request.put(`/notes/${id}`, data)
}

/**
 * 删除笔记(移入回收站)
 */
export function deleteNote(id) {
  return request.delete(`/notes/${id}`)
}

/**
 * 获取笔记历史记录
 */
export function getNoteHistory(params) {
  return request.get('/notes/history', { params })
}

/**
 * AI分析笔记
 */
export function aiAnalyzeNote(noteId) {
  return request.post(`/notes/${noteId}/ai-analyze`)
}

/**
 * 获取AI分析结果
 */
export function getAiAnalysis(noteId) {
  return request.get(`/notes/${noteId}/ai-result`)
}

/**
 * 创建批注
 */
export function createAnnotation(noteId, data) {
  return request.post(`/notes/${noteId}/annotations`, data)
}

/**
 * 获取笔记的批注列表
 */
export function getAnnotations(noteId) {
  return request.get(`/notes/${noteId}/annotations`)
}

/**
 * 更新批注
 */
export function updateAnnotation(annotationId, data) {
  return request.put(`/notes/annotations/${annotationId}`, data)
}

/**
 * 删除批注
 */
export function deleteAnnotation(annotationId) {
  return request.delete(`/notes/annotations/${annotationId}`)
}
