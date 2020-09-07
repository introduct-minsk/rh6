import Vue from 'vue'
import Vuex from 'vuex'
import { api } from '@/lib/ria-mailbox/api'
import { hwcrypto } from '@/lib/hwcrypto'
import { getField, updateField } from 'vuex-map-fields'
import { CachedValue } from '@/lib/CachedValue'
import * as breakpoints from '@/lib/ria-mailbox/screen-breakpoints'
// import * as mock from './mock'
// import { timeout } from '@/lib/ria-mailbox/timeout'

Vue.use(Vuex)


export let storeOptions = {
  strict: process.env.NODE_ENV !== 'production',
  state: {
    windowWidth: window.innerWidth,
    settings: new CachedValue({
      locale: 'ee',
    }),
    me: new CachedValue(),
    roles: undefined,
    messages: {
      in: new CachedValue(),
      out: new CachedValue(),
      search: new CachedValue({ content: [] }),  // todo: pre-shape others
    },
    draftSettings: {
      maxAttachmentNumber: 25,
      maxFileSizeBytes: 52428800,
    },
    newMessage: createDefaultNewMessageData(),
    newMessageInitedFromServer: false,
    pagination: {
      in: {
        currentPage: 1,
        perPage: 5,
      },
      out: {
        currentPage: 1,
        perPage: 5,
      },
      search: {
        currentPage: 1,
        perPage: 5,
      },
    },
    currentTab: 0,
    searchTerm: '',
    ui: {
      userSidebarShown: false,
      sideNavSidebarShown: false,
    },
    numAttachmentUploadRequests: 0,
  },
  getters: {
    getField,
    message: state => id => {
      return ['in', 'out', 'search'].map(direction =>
        state.messages[direction].value?.content.find(x => x.id === id),
      )
        .find(x => x)  // todo: there has to be a more elegant way
    },
    messagesIn(state) {
      return state.messages.in.value?.content || []  // todo: get rid of those
    },
    messagesOut(state) {
      return state.messages.out.value?.content || []
    },
    sidenavShown(state) {
      return state.windowWidth >= breakpoints.sidenav
    },
    isBusyWithFiles(state) {
      return state.newMessage.attachments.some(x => ['uploading', 'deleting'].includes(x.status))
    },
    numFilesLimitStatuses(state) {
      let limitLeft = state.draftSettings.maxAttachmentNumber
        - state.newMessage.attachments.filter(x => x.status === 'uploaded').length

      let statuses = state.newMessage.attachments.map(attachment => {
        if (attachment.status === 'uploaded') {
          return false  // already counted
        }
        if (attachment.status === 'tooLarge') {
          return // doesn’t count
        }
        if (limitLeft <= 0) {
          return true
        }
        --limitLeft
        return false
      })
      return { statuses, limitLeft }
    },
    isOffNumFilesLimit: (state, getters) => attachment => {
      return getters.numFilesLimitStatuses.statuses[state.newMessage.attachments.indexOf(attachment)]
    },
    hasReachedNumFilesLimit(state, getters) {
      return getters.numFilesLimitStatuses.limitLeft <= 0
    },
    userRepresentsThemselves(state) {
      return state.me.value.id === state.me.value.role.id
    },
  },
  mutations: {
    updateField,
    setValue(state, addressAndValue: Array<any>) {
      let value = addressAndValue.pop()
      addressAndValue.reduce((accum, val) => accum[val], state).set(value)
    },
    set(state, addressAndValue: Array<any>) {
      let value = addressAndValue.pop()
      let lastProp = addressAndValue.pop()
      addressAndValue.reduce((accum, val) => accum[val], state)[lastProp] = value
    },
    invalidate(state, address: Array<string>) {
      address.reduce((accum, val) => accum[val], state).invalidate()
    },
    setAttachmentProp(state, [attachment, prop, value]) {
      state.newMessage.attachments.find(x => x === attachment)[prop] = value
    },
    deleteAttachment(state, attachment) {
      state.newMessage.attachments = state.newMessage.attachments.filter(x => x !== attachment)
    },
    initNewMessage(state, draft) {
      let attachments = draft.attachments.map(x => ({ ...x, status: 'uploaded' }))
      Object.assign(state.newMessage, {
        to: draft.receiver || '',
        // to: '60001019906',
        subject: draft.subject || '',
        body: draft.text || '',
        attachments,
        signature: draft.signature,
      })
      state.newMessageInitedFromServer = true
    },
    setDraftSettings(state, value) {
      Object.assign(state.draftSettings, value)
    },
  },
  actions: {
    logout() {
      return api.logout()
    },
    async fetchMessages({ state, commit }, direction) {
      commit('invalidate', ['messages', direction])
      try {
        let messages = await api.getMessages(direction,
          state.pagination[direction].currentPage - 1,
          state.pagination[direction].perPage)
        commit('setValue', ['messages', direction, messages])
      } catch (error) {
        commit('setValue', ['messages', direction, { content: { error } }])
      }
    },
    async fetchMessagesAll({ dispatch }) {
      return Promise.all([
        dispatch('fetchMessages', 'in'),
        dispatch('fetchMessages', 'out'),
      ])
    },
    async fetchMe({ commit }) {
      commit('invalidate', ['me'])
      let me = await api.get('users', 'me')
      commit('setValue', ['me', me])
    },
    async fetchSettings({ commit }) {
      commit('invalidate', ['settings'])
      commit('setValue', ['settings', await api.get('users/me/settings')])
    },
    async init({ state, commit, dispatch }) {
      let ret
      let requests = [
        dispatch('fetchMe'),
        dispatch('fetchSettings'),
      ]
      try {
        let roles = await api.get('users/me/roles')
        await Promise.all(requests)
        if (!state.me.value.role) {
          commit('set', ['roles', roles])
          return
        }
      } catch (e) {
        if (e.response?.status?.toString().startsWith('5')) {
          ret = 'aar-problem'
          await Promise.all(requests)
          let role = { id: state.me.value.id }
          await api.post('users/me/role', role)
          commit('set', ['me', 'value', 'role', role])
        } else {
          throw e
        }
      }
      await Promise.all(requests)

      let [draftSettings] = await Promise.all([
        api.get('messages/draft/settings'),
        dispatch('fetchMessagesAll'),
      ])
      commit('setDraftSettings', draftSettings)

      return ret  // todo: more elegent
    },
    async initNewMessage({ commit }) {
      commit('initNewMessage', await api.get('messages/draft'))
    },
    async selectRole({ dispatch }, role) {
      await api.post('users/me/role', role)
      await dispatch('init')
    },
    async addAttachments({ state, commit, dispatch }, files: Array<File>) {
      let attachments = files.map(x => ({
        file: x,
        name: x.name,
        status: x.size > state.draftSettings.maxFileSizeBytes ? 'tooLarge' : 'waiting',
        error: undefined,
      }))
      commit('set', ['newMessage', 'attachments', [...state.newMessage.attachments, ...attachments]])
      await dispatch('uploadPendingAttachments')
    },
    async uploadPendingAttachments({ state, getters, commit }) {
      commit('set', ['numAttachmentUploadRequests', state.numAttachmentUploadRequests + 1])
      if (state.numAttachmentUploadRequests > 1) {
        return // todo
      }
      while (state.numAttachmentUploadRequests) {
        for (let attachment of [...state.newMessage.attachments]) {
          if (!state.newMessage.attachments.includes(attachment)) {
            continue  // deleted before had a chance to upload
          }
          if (attachment.status !== 'waiting') {
            continue
          }
          if (getters.isOffNumFilesLimit(attachment)) {
            continue
          }
          commit('setAttachmentProp', [attachment, 'status', 'uploading'])
          let url = 'messages/draft/attachments'
          if (!attachment.id) {
            url += '/upload'  // todo: change api
          }
          try {
            let upload = await api.upload(url, attachment.file, attachment.id || undefined)
            commit('setAttachmentProp', [attachment, 'id', upload.id])
            commit('setAttachmentProp', [attachment, 'status', 'uploaded'])
          } catch (e) {
            commit('setAttachmentProp', [attachment, 'status', 'error'])
            commit('setAttachmentProp', [attachment, 'error', e])
          }
          if (!state.newMessage.attachments.includes(attachment)) {
            continue  // deleted while uploading
          }
        }
        commit('set', ['numAttachmentUploadRequests', state.numAttachmentUploadRequests - 1])
      }
    },
    async deleteAttachment({ commit, dispatch }, attachment) {
      if (attachment.status === 'uploaded') {
        commit('setAttachmentProp', [attachment, 'status', 'deleting'])
        try {
          await api.delete('messages/draft/attachments', attachment.id)
        } catch (e) {
          commit('setAttachmentProp', [attachment, 'status', 'error'])
          commit('setAttachmentProp', [attachment, 'error', e])
        }
      }
      commit('deleteAttachment', attachment)
      await dispatch('uploadPendingAttachments')
    },
    async replaceAttachment({ state, commit, dispatch }, [replacee, file]) {
      commit('setAttachmentProp', [replacee, 'file', file])
      commit('setAttachmentProp', [replacee, 'name', file.name])
      // todo
      let status = file.size > state.draftSettings.maxFileSizeBytes ? 'tooLarge' : 'waiting'
      commit('setAttachmentProp', [replacee, 'status', status])
      await dispatch('uploadPendingAttachments')
    },
    async updateDraft({ state }) {
      let payload = {
        receiver: state.newMessage.to,
        subject: state.newMessage.subject,
        text: state.newMessage.body,
      }
      await api.update('messages', 'draft', payload)
    },
    async signDraft({ state, dispatch }) {
      let certificate = await hwcrypto.getCertificate({
        lang: state.settings.value.locale,
      })
      let signData = await api.get('messages/draft', ['sign/data'], {
        certInHex: certificate.hex,
      })
      let signature = await hwcrypto.sign(certificate, {
        type: 'SHA-256',
        hex: signData.hex,
      }, {
        lang: 'en',
      })
      await api.post(`messages/draft/sign`, {
        signatureInHex: signature.hex,
      })
      await dispatch('initNewMessage')  // to get a full signature obj
    },
    async cancelDraftSignature({ dispatch }) {
      await api.delete('messages/draft/sign')
      await dispatch('initNewMessage')
    },
    async dispatchDraft({ commit, dispatch }) {
      await api.post(`messages/draft/send`)
      commit('set', ['newMessage', createDefaultNewMessageData()])
      await dispatch('fetchMessagesAll')
    },
    async setLocale({ commit }, to) {
      commit('setValue', ['settings', {
        locale: to,
      }])
      return api.post('users/me/settings', {
        locale: to,
      })
    },
    async search({ state, commit }, term) {
      commit('invalidate', ['messages', 'search'])
      let results = await api.searchMessages(
        term,
        state.pagination.search.currentPage - 1,
        state.pagination.search.perPage,
      )
      commit('setValue', ['messages', 'search', results])
    },
  },
}

export function createStore() {
  let store = new Vuex.Store(storeOptions)

  // Object.assign(store.state, mock.state)

  window.addEventListener('resize',
    () => store.commit('set', ['windowWidth', window.innerWidth]))

  // todo: generalize; prove it’s a better approach than specific actions
  store.watch(state => state.pagination.in, () => {
    store.dispatch('fetchMessages', 'in')
  }, { deep: true })

  store.watch(state => state.pagination.out, () => {
    store.dispatch('fetchMessages', 'out')
  }, { deep: true })

  store.watch(state => state.pagination.search, () => {
    store.dispatch('search', store.state.searchTerm)
  }, { deep: true })

  return store
}


function createDefaultNewMessageData() {
  return {
    // todo: extract { to, subject, body } to a separate object
    id: undefined,
    signature: undefined,
    to: '',
    subject: '',
    body: '',
    validationTriggerred: false,
    attachments: [],
  }
}

const store = createStore()
export default store
