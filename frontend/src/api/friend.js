import request from '@/utils/request'

/**
 * 搜索好友
 */
export function searchFriend(keyword) {
  return request.post('/friend/friend', null, {
    params: { keyword }
  })
}

/**
 * 发送好友申请
 */
export function sendFriendRequest(targetUserId) {
  return request.post(`/friend/requests/${targetUserId}`)
}

/**
 * 处理好友申请
 */
export function handleFriendRequest(applyId, status) {
  return request.put(`/friend/requests/${applyId}`, null, {
    params: { status }
  })
}

/**
 * 获取好友列表
 */
export function getFriends(params) {
  return request.get('/friend', { params })
}

/**
 * 修改好友分组
 */
export function updateFriendGroup(friendId, groupName) {
  return request.put(`/friend/${friendId}/group`, null, {
    params: { groupName }
  })
}

/**
 * 获取好友申请列表
 */
export function getFriendRequests(params) {
  return request.get('/friend/requests', { params })
}
