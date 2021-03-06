import request from '@/router/axios'

const baseSubjectCategoryUrl = '/api/exam/v1/subjectCategory/'

export function fetchCategoryTree (query) {
  return request({
    url: baseSubjectCategoryUrl + 'categories',
    method: 'get',
    params: query
  })
}

export function fetchCategoryList (query) {
  return request({
    url: baseSubjectCategoryUrl + 'categoriesList',
    method: 'post',
    data: query
  })
}

export function questionBankChart (query) {
  return request({
    url: baseSubjectCategoryUrl + 'questionBankChart',
    method: 'post',
    data: query
  })
}

export function questionTypesChart (query) {
  return request({
    url: baseSubjectCategoryUrl + 'questionTypesChart',
    method: 'post',
    data: query
  })
}

export function difficultyLevelChart (query) {
  return request({
    url: baseSubjectCategoryUrl + 'difficultyLevelChart',
    method: 'post',
    data: query
  })
}

export function addCategory (obj) {
  return request({
    url: baseSubjectCategoryUrl,
    method: 'post',
    data: obj
  })
}

export function getCategory (id) {
  return request({
    url: baseSubjectCategoryUrl + id,
    method: 'get'
  })
}

export function delCategory (id) {
  return request({
    url: baseSubjectCategoryUrl + id,
    method: 'delete'
  })
}

export function putCategory (obj) {
  return request({
    url: baseSubjectCategoryUrl,
    method: 'put',
    data: obj
  })
}
