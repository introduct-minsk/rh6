import axios from 'axios'



export type Resource =
  | 'messages'
  | 'messages/draft'
  | 'messages/draft/settings'
  | 'messages/draft/sign'
  | 'messages/search'
  | 'users'
  | 'users/me/roles'
  | 'users/me/settings'

class ApiClient {
  private readonly base = '/api/v1'
  private readonly http = axios.create()
  private websocket: WebSocket

  constructor() {
    this.http.interceptors.response.use(undefined, error => {
      if (error.response?.status === 401) {
        window.location.replace('/oauth2/authorization/tara')
      }
      return Promise.reject(error)
    })
  }

  initWebsocket() {
    let protocol = window.location.protocol === 'http:' ? 'ws' : 'wss'
    this.websocket = new WebSocket(`${protocol}://${location.host}/websocket/messages/subscribe`)
  }

  onWebsocketMessage(callback: (ev: MessageEvent) => any) {
    if (!this.websocket) {
      this.initWebsocket()
    }
    this.websocket.onmessage = message => {
      let data = JSON.parse(message.data)
      callback(data)
    }
  }

  async get(resource: Resource, pathSegments: any | Array<string> = [], params = {}) {
    let path = [this.base, resource, ...[].concat(pathSegments)].join('/')
    let res = await this.http.get(path, { params })

    return res?.data
  }

  async post(path: string, body?) {
    let ret = await this.http.post(`${this.base}/${path}`, body)

    return ret.data
  }

  async upload(path: string, file, id?) {
    let formData = new FormData()
    formData.append('file', file)

    let method = id === undefined ? 'post' : 'put'
    if (id !== undefined) {
      path += `/${id}`
    }
    let ret = await this.http[method](`${this.base}/${path}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return ret.data
  }

  async update(resource: Resource, id: string, body) {
    return this.http.patch(`${this.base}/${resource}/${id}`, body)
  }

  async delete(path: string, id?: string) {
    if (id !== undefined) {
      path += `/${id}`
    }
    return this.http.delete(`${this.base}/${path}`)
  }

  async logout() {
    return this.http.post(`/oauth2/logout`)
  }

  async getMessages(
    direction: 'in' | 'out',
    page: number,
    perPage: number,
  ) {
    return this.get('messages', undefined, {
      direction: direction.toUpperCase(),
      page,
      size: perPage,
    })
  }

  async searchMessages(
    term: string,
    page: number,
    perPage: number,
  ) {
    return this.get('messages/search', undefined, {
      query: term,
      page,
      size: perPage,
    })
  }
}

export const api = new ApiClient()  // todo

if (process.env.NODE_ENV === 'development') {
  // keep the session alive
  // todo how to “uncompile” it here?
  setInterval(() => api.get('users/me/settings'), 3 * 60 * 1000)
}
