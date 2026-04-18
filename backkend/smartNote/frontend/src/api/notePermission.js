import request from '@/utils/request'

/**
 * 更新笔记权限
 */
export function updateNotePermission(noteId, data) {
  return request.put(`/notes/${noteId}/permission`, data)
}

/**
 * 获取笔记权限
 */
export function getNotePermission(noteId) {
  return request.get(`/notes/${noteId}/permission`)
}
