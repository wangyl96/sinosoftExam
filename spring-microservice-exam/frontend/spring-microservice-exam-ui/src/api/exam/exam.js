import request from '@/router/axios'
import { apiList } from '@/const/constant'

export function fetchList (query) {
  return request({
    url: apiList.exam + 'examinationList',
    method: 'get',
    params: query
  })
}

export function fetchSubjectListById (query) {
  return request({
    url: apiList.exam + 'subjectList',
    method: 'get',
    params: query
  })
}

export function getObj (id) {
  return request({
    url: apiList.exam + id,
    method: 'get'
  })
}

export function getSubjectIds (id, userId, query) {
  return request({
    url: apiList.exam + id + '/' + userId + '/subjectIds',
    method: 'get',
    params: query
  })
}

export function addObj (obj) {
  return request({
    url: apiList.exam,
    method: 'post',
    data: obj
  })
}

export function addExamRule (obj) {
  return request({
    url: apiList.exam + '/addExamRule',
    method: 'post',
    data: obj
  })
}

export function putObj (obj) {
  return request({
    url: apiList.exam,
    method: 'put',
    data: obj
  })
}

export function delObj (id) {
  return request({
    url: apiList.exam + id,
    method: 'delete'
  })
}

export function delAllObj (obj) {
  return request({
    url: apiList.exam + 'deleteAll',
    method: 'post',
    data: obj
  })
}

export function publicObj (obj) {
  return request({
    url: apiList.exam + 'examQuestionFlag',
    method: 'post',
    data: obj
  })
}

export function generateQrCode (id) {
  return request({
    url: apiList.exam + 'anonymousUser/generateQrCode/' + id,
    method: 'get'
  })
}
