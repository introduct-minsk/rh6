/* eslint-disable prefer-promise-reject-errors */
/* eslint-disable no-lone-blocks */
/* eslint-disable @typescript-eslint/no-unused-vars */

import { mount, shallowMount, createLocalVue, RouterLinkStub } from '@vue/test-utils'
import flushPromises from 'flush-promises'

import VueRouter from 'vue-router'
import Vuex from 'vuex'
import VueI18n from 'vue-i18n'
import BootstrapVue from 'bootstrap-vue'

import App from '@/App.vue'
import TheHeader from '@/components/TheHeader.vue'
import TheMailbox from '@/components/TheMailbox.vue'
import MessageList from '@/components/MessageList.vue'
import MailboxMessage from '@/components/MailboxMessage.vue'
import UserDropdown from '@/components/UserDropdown.vue'
import SignedBy from '@/components/SignedBy.vue'
import LocaleDropdown from '@/components/LocaleDropdown.vue'
import RoleSelector from '@/components/RoleSelector.vue'
import FileDownloadList from '@/components/FileDownloadList.vue'
import FileUploadList from '@/components/FileUploadList.vue'
import LogoutLink from '@/components/LogoutLink.vue'
import NewMessage from '@/components/NewMessage.vue'
import SearchBar from '@/components/SearchBar.vue'
import HomePage from '@/components/HomePage.vue'
import AppSideNav from '@/components/AppSideNav.vue'
import ErrorLabel from '@/components/ErrorLabel.vue'

import momentTz from 'moment-timezone'
import cloneDeep from 'lodash/cloneDeep'
import merge from 'lodash/merge'
import { hwcrypto } from '@/lib/hwcrypto'
import { formatEstonian } from '@/lib/datetime'
import { storeOptions, createStore } from '@/store'
import { CachedValue } from '@/lib/CachedValue'
import { getField, updateField } from 'vuex-map-fields'
import { timeout } from '@/lib/ria-mailbox/timeout'
import * as apiModule from '@/lib/ria-mailbox/api'  // NB: the mocked one



const api = apiModule.api as any  // todo: do it elegantly
jest.mock('@/lib/ria-mailbox/api', () => ({
  api: {
    get: jest.fn(() => Promise.resolve()),
    post: jest.fn(() => Promise.resolve()),
    upload: jest.fn(() => Promise.resolve()),
    update: jest.fn(() => Promise.resolve()),
    delete: jest.fn(() => Promise.resolve()),
    logout: jest.fn(() => Promise.resolve()),
    getMessages: jest.fn(() => Promise.resolve()),
    searchMessages: jest.fn(() => Promise.resolve()),

    onWebsocketMessage: jest.fn(),
  },
}))

const store = new Vuex.Store({
  ...storeOptions,
  state: {
    ...storeOptions.state,
    me: new CachedValue({
      role: { id: 'EE51001091072' },
    }, true),
    roles: [{ id: 'EE51001091072' }, { id: '70006317' }, { id: '11430169' }],
  },
})

const componentStub = {
  template: '<div class="stub"><slot /></div>',
}

const router = new VueRouter()
const localVue = createLocalVue()
localVue.use(BootstrapVue)
localVue.use(VueRouter)
localVue.use(Vuex)
localVue.use(VueI18n)
momentTz.tz.setDefault('Europe/Tallinn')  // so the tests run the same in a different TZ

const i18nMock = {
  $i18n: {
    locale: 'en',
    t: x => x,
    te: x => x,
  },
}
const commonMocks = {
  $t: x => x,
  moment: momentTz,
}
const commonOptions = {
  mocks: commonMocks,
}

describe('MailboxMessage.vue', () => {
  let storeOpts = {
    state: {
      me: new CachedValue({ role: { id: 'EE60001019906' } }, true),
    },
    actions: {
      fetchMessagesAll: jest.fn(),
    },
  }
  let mountOpions = {
    mocks: {
      ...commonMocks,
      $router: {
        push: jest.fn(),
      },
      $route: {
        params: {
          tab: 'in',
        },
      },
    },
    store: new Vuex.Store(storeOpts),
    filters: {
      formatEstonian,
    },
  }
  const wrapper = shallowMount(MailboxMessage, mountOpions)

  it('calls the api', async () => {
    await flushPromises()
    // api.get.mockImplementationOnce(() => Promise.resolve())
    expect(api.get).toBeCalledTimes(1)
  })

  it('dispatches fetchMessagesAll()', async () => {
    await flushPromises()
    expect(storeOpts.actions.fetchMessagesAll).toBeCalledTimes(1)
  })

  it('renders what was passed in a correct format', async () => {
    await wrapper.setData({
      message: {
        id: '27bb0858-b59f-49c5-af30-892f337437c8',
        sender: {
          id: 'EE10101010005',
          firstName: 'DEMO',
          lastName: 'SMART-ID',
        },
        receiver: {
          id: 'EE60001019906',
          firstName: 'O\'CONNEŽ-ŠUSLIK TESTNUMBER',
          lastName: 'MARY ÄNN',
        },
        type: 'SIMPLE',
        subject: 'test',
        unread: false,
        createdOn: '2020-04-17T12:23:34.56004Z',
      },
    })
    expect(wrapper.text()).toBe('test From: DEMO SMART-ID (EE10101010005) onBehalfOf 17.04.2020, 15:23    Back')
  })

  it('renders a notification', async () => {
    await wrapper.setData({
      message:
        { id: '24925ead-0045-4a17-a4b7-575dc348069d', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'NOTIFICATION', subject: 'READ', unread: false, createdOn: '2020-06-18T10:36:28.023008Z', related: { id: '7f6102d3-b367-4457-bf80-fa0627003b57', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Fiélsé Kirmesdag æss ðö, zé méi Fläiß wellen. Hu hire blénken prächteg oft. Dir Stad Stret en, op ðéi Hunn dénen botze, si sin gutt durch kille. Néi wuel Gart lossén ke. Úm dén Wísen welle Schuebersonnðeg, ðem ma\'n Fläiß gröussé eÞ. Fir Hären göung Hämmel', unread: false, createdOn: '2020-06-18T10:34:48.539694Z' } },
    })
    expect(wrapper.text()).toMatch('readReceiptShort From: OSKAR RIATEST')
  })

  it('renders the attachment list', async () => {
    expect(wrapper.find('.attachment-list').element).toBeFalsy()
    await wrapper.setData({
      message:
        { id: '63143d37-be88-44e4-b8f3-247e9866c880', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', subject: 'pealkiri', unread: false, createdOn: '2020-06-18T15:41:52.926194Z', attachments: [{ id: '77747b97-03f9-40a4-875b-989f64bbcf17', name: 'Nët an Hælm Fríémð.jpg' }], signature: { valid: true, signedBy: 'PAJOR,HELEN,49101056014', signingTime: '2020-06-18T15:41:30Z' } },
    })
    expect(wrapper.find('.attachment-list').element).toBeTruthy()
  })

  it('renders outgoing message correctly', async () => {
    await wrapper.setData({
      message:
        { id: '14383921-ee79-40c9-a428-5b9709cd4675', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: '12249934' }, type: 'SIMPLE', subject: 'jou', unread: false, createdOn: '2020-06-18T14:42:45.411199Z', text: 'meh', attachments: [{ id: '3295f77c-ae62-4bb0-88b0-cb81b7b0b0ad', name: 'why worry slide.jpeg' }], signature: { valid: true, signedBy: 'PAJOR,HELEN,49101056014', signingTime: '2020-06-18T14:30:40Z' } },
    })
    expect(wrapper.text()).toMatch(`jou To: MARY ÄNN O'CONNEŽ-ŠUSLIK`)
  })

  it('brings to outbox on push to back', async () => {
    expect(mountOpions.mocks.$router.push).not.toBeCalled()
    wrapper.find('button').trigger('click')
    expect(mountOpions.mocks.$router.push).toBeCalledWith(`/mailbox/${mountOpions.mocks.$route.params.tab}`)
  })

  it('handles messages to oneself', async () => {
    await wrapper.setData({
      message:
        { id: '3231c9e5-0ccd-4ce1-9d7b-bf0abb6548f7', subject: 'foosubject', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', unread: false, createdOn: '2020-06-18T12:13:06.20619Z' },
    })
    expect(wrapper.text()).toMatch(`foosubject From: MARY ÄNN O'CONNEŽ-ŠUSLIK`)
  })

  it('handles messages without a subject', async () => {
    await wrapper.setData({
      message:
        { id: '3231c9e5-0ccd-4ce1-9d7b-bf0abb6548f7', subject: '', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', unread: false, createdOn: '2020-06-18T12:13:06.20619Z' },
    })
    expect(wrapper.text()).toMatch(`(No Subject)`)
  })

  it('shows an error on api exception', async () => {
    expect(wrapper.find('errorlabel-stub').element).toBeFalsy()
    api.get.mockImplementationOnce(() => Promise.reject())
    let wrapper2 = shallowMount(MailboxMessage, mountOpions)
    await flushPromises()
    expect(wrapper2.find('errorlabel-stub').element).toBeTruthy()
  })
})

describe('SignedBy.vue', () => {
  let wrapper = shallowMount(SignedBy, {
    ...commonOptions,
    propsData: {
      signature: {
        valid: true,
        signedBy: 'PAJOR,HELEN,49101056014',
        signingTime: '2020-06-18T15:41:30Z',
      },
      messageId: '63143d37-be88-44e4-b8f3-247e9866c880',
    },
  })
  it('renders what was passed to props', async () => {
    expect(wrapper.text().replace(/[\s\n]+/g, ' ')).toBe('Signed with digital ID: Helen Pajor (49101056014) signedByAt 18.06.2020 18:41:30 | digidocLink')
    expect(wrapper.find('.caution').element).toBeFalsy()
  })

  it('renders an error on invalid signature', async () => {
    await wrapper.setProps({
      signature: {
        valid: false,
        signedBy: 'PAJOR,HELEN,49101056014',
        signingTime: '2020-06-18T15:41:30Z',
      },
    })
    expect(wrapper.find('.caution').element).toBeTruthy()
  })

  it('handles valid: null', async () => {
    await wrapper.setProps({
      signature: {
        valid: null,
        signedBy: 'PAJOR,HELEN,49101056014',
        signingTime: '2020-06-18T15:41:30Z',
      },
    })
    expect(wrapper.find('.caution').text()).toBe('validationServiceDown')
  })
})

describe('RoleSelector.vue', () => {
  let selectRole = jest.fn()
  let wrapper = shallowMount(RoleSelector, {
    ...commonOptions,
    store: new Vuex.Store({
      state: {
        roles: [
          { id: 'id1' },
          { id: 'id2' },
        ],
      },
      actions: {
        selectRole,
      },
    }),
    stubs: {
      BListGroupItem: componentStub,
    },
  })

  it('renders roles in store', async () => {
    expect(wrapper.find('.stub').text()).toBe('id1')  // todo
  })

  it('on click, calls the action to set a role', async () => {
    wrapper.find('.stub').vm.$emit('click')
    await wrapper.vm.$nextTick()
    expect(selectRole).toBeCalled()
  })
})

describe('FileDownloadList.vue', () => {
  let wrapper = mount(FileDownloadList, {
    ...commonOptions,
    propsData: {
      files: [{ name: 'Nët an Hælm Fríémð.jpg', url: '/api/v1/messages/63143d37-be88-44e4-b8f3-247e9866c880/attachments/77747b97-03f9-40a4-875b-989f64bbcf17' }],
    },
  })
  it('renders a file table', async () => {
    expect(wrapper.text()).toMatch('NameLinkNët an Hælm Fríémð.jpgsave_alt')
  })
})

describe('AppSideNav.vue', () => {
  let wrapper = mount(AppSideNav, {
    ...commonOptions,
    store,
    mocks: {
      ...commonMocks,
      $route: {},
    },
  })
  it('has a single item called “Mailbox”', async () => {
    expect(wrapper.find('.active span.title').text()).toBe('Mailbox')
  })
})

describe('FileUploadList.vue', () => {
  let mocks = {
    $store: {
      dispatch: jest.fn(),
    },
  }
  let wrapper = mount(FileUploadList, {
    ...commonOptions,
    mocks: {
      ...commonMocks,
      ...mocks,
    },
    propsData: {
      files: [{ file: new File([], 'filename'), name: 'on_the_verge_of_limit', status: 'uploaded', error: '__vue_devtool_undefined__', id: 'f1fda2f0-e25b-4f06-ba3e-6da124f21b37', object: { file: new File([], 'filename'), name: 'on_the_verge_of_limit', status: 'uploaded', error: '__vue_devtool_undefined__', id: 'f1fda2f0-e25b-4f06-ba3e-6da124f21b37' }, numFilesLimitStatus: false }, { file: new File([], 'filename'), name: 'fELJxjmAjSI.jpg', status: 'uploading', error: '__vue_devtool_undefined__', object: { file: new File([], 'filename'), name: 'fELJxjmAjSI.jpg', status: 'uploading', error: '__vue_devtool_undefined__' }, numFilesLimitStatus: false }, { file: new File([], 'filename'), name: 'ніпонятна картінка довга назва  дада и букви raznye PODIJUHV}[øˆø¨¨¥∂bnbnn  oue nazva nazva nazvannie.jpg', status: 'waiting', error: '__vue_devtool_undefined__', object: { file: new File([], 'filename'), name: 'ніпонятна картінка довга назва  дада и букви raznye PODIJUHV}[øˆø¨¨¥∂bnbnn  oue nazva nazva nazvannie.jpg', status: 'waiting', error: '__vue_devtool_undefined__' }, numFilesLimitStatus: false }, { file: new File([], 'filename'), name: 'Andke mõned umlaudid palun lapsele, jah. Kallis eestlane, kus te naljakaid tähti vajate.jpg', status: 'waiting', error: '__vue_devtool_undefined__', object: { file: new File([], 'filename'), name: 'Andke mõned umlaudid palun lapsele, jah. Kallis eestlane, kus te naljakaid tähti vajate.jpg', status: 'waiting', error: '__vue_devtool_undefined__' }, numFilesLimitStatus: false }, { file: new File([], 'filename'), name: 'some_planes_like_dudes____and_the_file_name_is_nobr_loooooooooooooooooooooong_come_on_break_the_layout.jpg', status: 'waiting', error: '__vue_devtool_undefined__', object: { file: new File([], 'filename'), name: 'some_planes_like_dudes____and_the_file_name_is_nobr_loooooooooooooooooooooong_come_on_break_the_layout.jpg', status: 'waiting', error: '__vue_devtool_undefined__' }, numFilesLimitStatus: false }, { file: new File([], 'filename'), name: 'A9-bqR5ROe4.jpg', status: 'waiting', error: '__vue_devtool_undefined__', object: { file: new File([], 'filename'), name: 'A9-bqR5ROe4.jpg', status: 'waiting', error: '__vue_devtool_undefined__' }, numFilesLimitStatus: false }, { file: new File([], 'filename'), name: '02-1-_misto-_chastyna_1-_glava_i-_demo.rar', status: 'waiting', error: '__vue_devtool_undefined__', object: { file: new File([], 'filename'), name: '02-1-_misto-_chastyna_1-_glava_i-_demo.rar', status: 'waiting', error: '__vue_devtool_undefined__' }, numFilesLimitStatus: false }, { file: new File([], 'filename'), name: 'eestiüäölmnjdbwqon cüö.svg', status: 'waiting', error: '__vue_devtool_undefined__', object: { file: new File([], 'filename'), name: 'eestiüäölmnjdbwqon cüö.svg', status: 'waiting', error: '__vue_devtool_undefined__' }, numFilesLimitStatus: false }],
      disabled: false,
    },
  })

  it('renders ok', async () => {
    ; (wrapper.vm as any).replaceAttachment({})
    expect(wrapper.text().startsWith('NameStatusToolson_the_verge_of_limituploadStatuses.uploaded check_circle_outlinedelete_outline Delete edit ChangefELJxjmAjSI.jpguploadStatuses.uploading'))
      .toBeTruthy()
  })
  it('does nothing on empty files replacement', async () => {
    ; (wrapper.vm as any).onReplacementSelected([])
    expect(mocks.$store.dispatch).not.toBeCalled()
  })
  it('dispatches replaceAttachment on file input', async () => {
    ; (wrapper.vm as any).onReplacementSelected([new File([], 'foo')])
    expect(mocks.$store.dispatch).toBeCalled()
  })
  it('generates correct status text', async () => {
    expect((wrapper.vm as any).statusText({
      status: 'error',
      error: {
        response: {
          data: {
            error: 'errorFoo',
          },
        },
      },
    })).toBe('Error: errorFoo')
  })
})

describe('HomePage.vue', () => {
  let wrapper = shallowMount(HomePage, {
    ...commonOptions,
    localVue,
    store,
    router,
  })
  it('has router-view', async () => {
    expect(wrapper.find('router-view-stub').element).toBeTruthy()
  })
})

describe('LogoutLink.vue', () => {
  {
    let wrapper = shallowMount(LogoutLink, {
      ...commonOptions,
      store: new Vuex.Store({
        state: {
          me: new CachedValue(),
        },
      }),
    })
    // it('is disabled when the user is logged out', () => {
    //   expect(wrapper.find('blink-stub').attributes('disabled')).toBeTruthy()
    // })
  }
  {
    let actions = {
      logout: jest.fn(),
    }
    let wrapper = mount(LogoutLink, {
      ...commonOptions,
      store: new Vuex.Store({
        state: {
          me: new CachedValue({}, true),
        },
        actions,
      }),
    })
    Object.defineProperty(window, 'location', {
      value: {
        assign: jest.fn(),
      },
    })

    it('is enabled when the user is logged in', async () => {
      expect(wrapper.find('a').attributes('disabled')).toBeFalsy()
    })
    it('dispatches logout on click', async () => {
      // todo: click instead
      await (wrapper.vm as any).logout()
      expect(actions.logout).toHaveBeenCalled()
    })
    let afterLogoutRedirectTo = '/oauth2/authorization/tara'
    it(`on click, brings the user to "${afterLogoutRedirectTo}"`, async () => {
      // todo: click instead after jest resolves its problem
      await (wrapper.vm as any).logout()
      // wrapper.find('a').trigger('click', {})
      expect(window.location.assign).toHaveBeenCalledWith(afterLogoutRedirectTo)
    })
  }
})

describe('TheMailbox.vue', () => {
  let mocks = {
    $router: {
      push: jest.fn(() => Promise.resolve()),
      replace: jest.fn(() => Promise.resolve()),
    },
    $route: {
      params: {
        tab: 'unread',
      },
      path: '/mailbox/unread',
    },
    $bvToast: {
      show: jest.fn(),
    },
  }
  let messagesIn = [{ id: '8168f068-7f9c-4536-808e-3447abbe2bf2', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', subject: 'a message to myself', unread: true, createdOn: '2020-06-24T18:31:53.356147Z' }, { id: 'f00b7921-53d3-432a-b7e3-5319e47d7f2f', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'NOTIFICATION', subject: 'READ', unread: true, createdOn: '2020-06-23T18:46:42.582396Z', related: { id: '033055e4-9bef-4934-b391-ffa9b84dc849', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'zizu test', unread: false, createdOn: '2020-06-22T15:08:41.150108Z' } }, { id: '7cbe9baa-75a0-4cb3-b018-3d385c20d875', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'NOTIFICATION', subject: 'READ', unread: true, createdOn: '2020-06-23T18:46:07.649232Z', related: { id: 'a1631a70-0f7a-467c-ae36-c0bae3cb5446', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Auto test 22-06-2020 18:13 from 60001019906 to 10101010005', unread: false, createdOn: '2020-06-22T15:14:08.487727Z' } }, { id: 'd0514d01-6130-4096-948d-8294751fafd4', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'NOTIFICATION', subject: 'READ', unread: false, createdOn: '2020-06-23T12:10:43.779589Z', related: { id: 'c60a81d4-8cdc-42e5-9ee9-83c8667c91dc', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Test 23.06 (1)', unread: false, createdOn: '2020-06-23T12:10:01.005733Z' } }, { id: 'aa218152-ac90-4da2-b063-db1350109c87', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', subject: 'Test 23.06 (2)', unread: true, createdOn: '2020-06-23T12:10:25.413571Z' }]
  let messagesOut = [{ id: '17a3117e-6b41-423d-bacb-05ff90751bbc', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'a message to oskar', unread: true, createdOn: '2020-06-24T18:32:36.441375Z' }, { id: '8168f068-7f9c-4536-808e-3447abbe2bf2', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', subject: 'a message to myself', unread: true, createdOn: '2020-06-24T18:31:53.356147Z' }, { id: '0e95660e-2318-47c2-9056-fb1b31860f2e', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Test 23.06 (4)', unread: true, createdOn: '2020-06-23T20:24:26.028091Z' }, { id: 'c60a81d4-8cdc-42e5-9ee9-83c8667c91dc', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Test 23.06 (1)', unread: false, createdOn: '2020-06-23T12:10:01.005733Z' }, { id: 'a20c4fb9-6d1a-4ee9-9118-25fc230c3ca2', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Auto test 22-06-2020 18:34 from 60001019906 to 10101010005', unread: false, createdOn: '2020-06-22T15:35:23.430325Z' }]

  let storeOpts = {
    state: {
      messages: {
        in: new CachedValue(messagesIn, true),
        out: new CachedValue(messagesOut, true),
        search: new CachedValue({ content: [] }, true),
      },
      searchTerm: '',
      pagination: {
        in: {
          currentPage: 1,
        },
      },
      currentTab: 0,
    },
    getters: {
      getField,
      messagesIn(state) {
        return state.messages.in.value || []
      },
      messagesOut(state) {
        return state.messages.out.value || []
      },
    },
    mutations: {
      updateField,
      set: jest.fn(),
    },
    actions: {
      dispatch: jest.fn(),
      fetchMessages: jest.fn(),
    },
  }
  let store = new Vuex.Store(storeOpts)
  let wrapper = shallowMount(TheMailbox, {
    ...commonOptions,
    router,
    store,
    mocks: {
      ...commonMocks,
      ...mocks,
    },
  })

  it('renders the inbox tab', async () => {
    expect(wrapper.find('btabs-stub').attributes('value')).toBe('0')
  })

  it('does nothing on a strange websocket message', async () => {
    (wrapper.vm as any).onWebsocketMessage({ type: 'FOO' })
    expect(mocks.$bvToast.show).not.toBeCalled()
  })

  it('shows a toast on websocket message', async () => {
    (wrapper.vm as any).onWebsocketMessage({ type: 'MESSAGE_RECEIVED' })
    expect(mocks.$bvToast.show).toBeCalledWith('my-toast')
  })

  it('refreshes the inbox on websocket message if on the first page of the inbox', async () => {
    mocks.$route.path = '/mailbox/in'
    { (wrapper.vm as any).onWebsocketMessage({ type: 'MESSAGE_RECEIVED' }) }
    expect(storeOpts.actions.fetchMessages).toBeCalled()
    await flushPromises()
    store.state.currentTab = 1
    await flushPromises()
    store.state.currentTab = 0
    mocks.$router.push.mockImplementationOnce(() => Promise.reject({}))
    await flushPromises()
  })

  {
    let mocks2 = cloneDeep(mocks)
    mocks2.$route.params.tab = 'search'
    let wrapper = shallowMount(TheMailbox, {
      ...commonOptions,
      router,
      store,
      mocks: {
        ...commonMocks,
        ...mocks2,
      },
    })

    it('renders the inbox tab anyway', async () => {
      expect(wrapper.find('btabs-stub').attributes('value')).toBe('0')
    })
  }

  {
    let mocks2 = cloneDeep(mocks)
    mocks2.$route.params.tab = 'foo'
    let store = new Vuex.Store(cloneDeep(storeOpts))
    let wrapper = shallowMount(TheMailbox, {
      ...commonOptions,
      router,
      store,
      mocks: {
        ...commonMocks,
        ...mocks2,
      },
    })

    it('doesn’t crash on an unexpected tab', async () => {
      await flushPromises()
    })
  }
})

describe('App.vue', () => {
  const createWrapper = (storeOptsMerge = {}, optsMerge = {}) => {
    let storeOpts = {
      state: {
        me: new CachedValue({}, true),
        settings: new CachedValue({ locale: 'en' }, true),
      },
      actions: {
        init: jest.fn(() => 'aar-problem'),
      },
    }

    merge(storeOpts, storeOptsMerge)

    let opts = {
      ...commonOptions,
      router,
      store: new Vuex.Store(storeOpts),
      mocks: {
        ...commonMocks,
        ...i18nMock,
        $bvToast: {
          toast: () => undefined,
        },
      },
    }
    merge(opts, optsMerge)
    let wrapper = shallowMount(App, opts)

    return { storeOpts, opts, wrapper }
  }

  {
    let { wrapper, storeOpts } = createWrapper()
    it('mounts', async () => {
      expect(wrapper.isVueInstance()).toBeTruthy()
    })
    it('calls init()', async () => {
      expect(storeOpts.actions.init).toBeCalledTimes(1)
    })
  }

  {
    let { wrapper } = createWrapper({ actions: { init: jest.fn(() => Promise.reject()) as any } })
    it('shows an error on init()’s exception', async () => {
      await wrapper.vm.$nextTick()
      expect(wrapper.find('globalerror-stub').element).toBeTruthy()
    })
  }

  {
    let { wrapper } = createWrapper({
      actions: {
        init: jest.fn(() => Promise.reject({
          response: { status: 401 },
        })) as any,
      },
    })
    it('doesn’t show an error on 401', async () => {
      await wrapper.vm.$nextTick()
      expect(wrapper.find('globalerror-stub').element).toBeFalsy()
    })
  }

  {
    let { wrapper } = createWrapper({ actions: { init: jest.fn(() => Promise.resolve('foo')) as any } })
    it('ignores other init()’s return values', async () => {
      await wrapper.vm.$nextTick()
      expect(wrapper.find('globalerror-stub').element).toBeFalsy()
    })
  }
})

describe('SearchBar.vue', () => {
  let storeOpts = {
    state: {
      searchTerm: 'foo',
    },
    getters: {
      getField,
    },
    mutations: {
      updateField,
      setSearch(state, value) {
        state.searchTerm = value
      },
    },
    actions: {
      search: jest.fn(() => Promise.resolve()),
    },
  }
  let opts = {
    ...commonOptions,
    store: new Vuex.Store(storeOpts),
    mocks: {
      ...commonMocks,
      ...i18nMock,
      $bvToast: {
        toast: jest.fn(),
      },
      $router: {
        push: jest.fn(() => Promise.resolve()),
      },
    },

  }
  {
    let wrapper = mount(SearchBar, opts)

    it('renders an input with the value from the store', async () => {
      await wrapper.vm.$nextTick()
      expect((wrapper.find('input').element as any).value).toBe('foo')
    })

    it('changes route to /mailbox/search on input and dispatches search', async () => {
      opts.store.commit('setSearch', 'bar')
      await wrapper.vm.$nextTick()
      expect((wrapper.find('input').element as any).value).toBe('bar')
      await timeout(1000)
      await flushPromises()  // just in case
      expect(opts.mocks.$router.push).toBeCalledWith('/mailbox/search')
      expect(storeOpts.actions.search).toBeCalled()
    })
  }

  {
    let opts2 = { ...opts }
    opts2.mocks.$router.push = jest.fn(() => Promise.reject({}))
    let storeOpts2 = cloneDeep(storeOpts)
    storeOpts2.actions.search = jest.fn(() => Promise.reject({}))
    opts2.store = new Vuex.Store(storeOpts2)
    let wrapper = mount(SearchBar, opts2)
    it('handles errors', async () => {
      opts2.store.commit('setSearch', 'baz')
      await timeout(1000)
      // no unhandled exceprions === success
    })
  }

  {
    let opts2 = {
      ...opts,
      store: new Vuex.Store(cloneDeep(storeOpts)),
    }
    let wrapper = mount(SearchBar, opts2)
    it('handles errors', async () => {
      opts2.store.commit('setSearch', '')
      await timeout(1000)
      // expect(opts2.mocks.$router.push).not.toBeCalled()
    })
  }
})

describe('MessageList.vue', () => {
  let messagesIn = [{ id: '8168f068-7f9c-4536-808e-3447abbe2bf2', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', subject: 'a message to myself', unread: true, createdOn: '2020-06-24T18:31:53.356147Z' }, { id: 'f00b7921-53d3-432a-b7e3-5319e47d7f2f', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'NOTIFICATION', subject: 'READ', unread: true, createdOn: '2020-06-23T18:46:42.582396Z', related: { id: '033055e4-9bef-4934-b391-ffa9b84dc849', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'zizu test', unread: false, createdOn: '2020-06-22T15:08:41.150108Z' } }, { id: '7cbe9baa-75a0-4cb3-b018-3d385c20d875', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'NOTIFICATION', subject: 'READ', unread: true, createdOn: '2020-06-23T18:46:07.649232Z', related: { id: 'a1631a70-0f7a-467c-ae36-c0bae3cb5446', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Auto test 22-06-2020 18:13 from 60001019906 to 10101010005', unread: false, createdOn: '2020-06-22T15:14:08.487727Z' } }, { id: 'd0514d01-6130-4096-948d-8294751fafd4', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'NOTIFICATION', subject: 'READ', unread: false, createdOn: '2020-06-23T12:10:43.779589Z', related: { id: 'c60a81d4-8cdc-42e5-9ee9-83c8667c91dc', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Test 23.06 (1)', unread: false, createdOn: '2020-06-23T12:10:01.005733Z' } }, { id: 'aa218152-ac90-4da2-b063-db1350109c87', sender: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST', roleId: 'EE10101010005' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', subject: 'Test 23.06 (2)', unread: true, createdOn: '2020-06-23T12:10:25.413571Z' }]
  let messagesOut = [{ id: '17a3117e-6b41-423d-bacb-05ff90751bbc', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'a message to oskar', unread: true, createdOn: '2020-06-24T18:32:36.441375Z' }, { id: '8168f068-7f9c-4536-808e-3447abbe2bf2', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK' }, type: 'SIMPLE', subject: 'a message to myself', unread: true, createdOn: '2020-06-24T18:31:53.356147Z' }, { id: '0e95660e-2318-47c2-9056-fb1b31860f2e', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Test 23.06 (4)', unread: true, createdOn: '2020-06-23T20:24:26.028091Z' }, { id: 'c60a81d4-8cdc-42e5-9ee9-83c8667c91dc', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Test 23.06 (1)', unread: false, createdOn: '2020-06-23T12:10:01.005733Z' }, { id: 'a20c4fb9-6d1a-4ee9-9118-25fc230c3ca2', sender: { id: 'EE60001019906', firstName: 'MARY ÄNN', lastName: 'O\'CONNEŽ-ŠUSLIK', roleId: 'EE60001019906' }, receiver: { id: 'EE10101010005', firstName: 'OSKAR', lastName: 'RIATEST' }, type: 'SIMPLE', subject: 'Auto test 22-06-2020 18:34 from 60001019906 to 10101010005', unread: false, createdOn: '2020-06-22T15:35:23.430325Z' }]

  let $store = {
    state: {
      me: new CachedValue({ role: { id: 'EE60001019906' } }, true),
      pagination: { in: { currentPage: 1, perPage: 5 }, out: { currentPage: 1, perPage: 5 }, search: { currentPage: 1, perPage: 5 } },
      messages: {
        in: new CachedValue({ totalElements: 573 }, true),
        out: new CachedValue({ totalElements: 592 }, true),
      },
    },
    commit: jest.fn(),
  }

  let wrapper = shallowMount(MessageList, {
    ...commonOptions,
    mocks: {
      ...commonMocks,
      $store,
    },
    propsData: {
      messages: messagesIn,
      direction: 'in',
    },
    filters: {
      formatEstonian,
    },
    stubs: {
      RouterLink: RouterLinkStub,
    },
  })

  it('renders inbox', async () => {
    expect(wrapper.find('.to-or-from').text()).toBe(`From: MARY ÄNN O'CONNEŽ-ŠUSLIK (EE60001019906)`)
  })
  it('renders outbox', async () => {
    await wrapper.setProps({ messages: messagesOut, direction: 'out' })
    expect(wrapper.find('.to-or-from').text()).toBe('To: OSKAR RIATEST (EE10101010005)')
  })
  it('commits pagination to the store', async () => {
    (wrapper.vm as any).currentPage = 2
    await wrapper.vm.$nextTick()
    expect($store.commit).toBeCalledTimes(1)
  })
  it('commits perPage to the store', async () => {
    (wrapper.vm as any).perPage = 10
    await wrapper.vm.$nextTick()
    expect($store.commit).toBeCalledTimes(2)
  })
})

describe('store', () => {
  let store = createStore()
  it('has state', () => {
    expect(store.state).toBeTruthy()
  })
})

describe('NewMessage.vue', () => {
  let createMockStoreOptions = () => ({
    state: {
      draftSettings: cloneDeep(storeOptions.state.draftSettings),
      newMessageInitedFromServer: true,
      newMessage: {
        id: '__vue_devtool_undefined__',
        signature: null,
        to: '123',
        subject: 'foo subject',
        body: 'foo text',
        validationTriggerred: false,
        attachments: [
          { file: new File([], 'name2'), name: 'eestiüäölmnjdbwqon cüö.svg', status: 'uploaded', id: '1b7f7fa6-1d2d-49e6-85ab-a1fbd6d68d4e' },
          { file: new File([], 'name2'), name: 'A9-bqR5ROe4.jpg', status: 'waiting' },
        ],
      },
    },
    getters: {
      getField,
      numFilesLimitStatuses: () => ({ statuses: [false, false] }),
      hasReachedNumFilesLimit: () => false,
      isBusyWithFiles: () => false,
    },
    mutations: {
      updateField,
    },
    actions: {
      initNewMessage: jest.fn(),
      updateDraft: jest.fn(),
      dispatchDraft: jest.fn(),
      cancelDraftSignature: jest.fn(),
      signDraft: jest.fn(),
      addAttachments: jest.fn(),
    },
  })
  const createWrapperOpts = storeOpts => ({
    ...commonOptions,
    store: new Vuex.Store(storeOpts),
    directives: {
      mask: () => undefined,
    },
    mocks: {
      ...commonMocks,
      ...i18nMock,
      $bvToast: {
        toast: () => undefined,
      },
      $router: {
        push: jest.fn(),
      },
    },
  })
  let mockStoreOptions = createMockStoreOptions()
  let wrapperOpts = createWrapperOpts(mockStoreOptions)
  let wrapper = shallowMount(NewMessage, wrapperOpts)


  it('dispatches `initNewMessage` on creation', async () => {
    expect(mockStoreOptions.actions.initNewMessage).toBeCalledTimes(1)
  })

  it('throttles onInput()', async () => {
    ; (wrapper.vm as any).onInput()
    await timeout(10)
    expect(mockStoreOptions.actions.updateDraft).not.toBeCalled()
    await timeout(2000)
    expect(mockStoreOptions.actions.updateDraft).toBeCalled()
  })

  it('dispatches `signDraft()` on sign button click', async () => {
    wrapper.find('.sign-button').vm.$emit('click')
    await flushPromises()
    expect(mockStoreOptions.actions.signDraft).toBeCalledTimes(1)
  })

  it('reports an error on `signDraft()` exceptions', async () => {
    mockStoreOptions.actions.signDraft.mockImplementationOnce(() => Promise.reject({}))
    wrapper.find('.sign-button').vm.$emit('click')
    await flushPromises()
    expect(wrapper.vm.$data.status).toBe(undefined)
    // todo
  })

  it('does nothing on hwcrypto.USER_CANCEL thrown from `signDraft()`', async () => {
    mockStoreOptions.actions.signDraft.mockImplementationOnce(() => Promise.reject({ message: hwcrypto.USER_CANCEL }))
    wrapper.find('.sign-button').vm.$emit('click')
    await flushPromises()
    expect(wrapper.vm.$data.status).toBe(undefined)
  })

  it('reports a hwcrypto error on other hwcrypto exceptions thrown from `signDraft()`', async () => {
    mockStoreOptions.actions.signDraft.mockImplementationOnce(() => Promise.reject({ message: hwcrypto.NO_IMPLEMENTATION }))
    wrapper.find('.sign-button').vm.$emit('click')
    await flushPromises()
    // todo: mock reportError() from the lib
    expect(wrapper.vm.$data.status).toBe(undefined)
  })

  it('dispatches `addAttachments()` on addAttachments()', async () => {
    ; (wrapper.vm as any).addAttachments([])
    expect(mockStoreOptions.actions.addAttachments).toBeCalledTimes(1)
  })

  it('dispatches `dispatch()` on [Send] click', async () => {
    wrapper.find('veerabutton-stub[data-test-id="send-button"]').vm.$emit('click')
    await flushPromises()
    expect(mockStoreOptions.actions.dispatchDraft).toBeCalledTimes(1)
  })

  it('reports an error if dispatchDraft() throws', async () => {
    mockStoreOptions.actions.dispatchDraft.mockImplementationOnce(() => Promise.reject({}))
    ; (wrapper.vm as any).dispatch()
    await flushPromises()
    // todo: mock reportError() from the lib
  })

  it('reports an error if dispatchDraft() throws', async () => {
    mockStoreOptions.actions.updateDraft.mockImplementationOnce(() => Promise.reject({}))
    ; (wrapper.vm as any).onInput()
    await timeout(1500)
    await flushPromises()
    // todo: mock reportError() from the lib
  })

  it('reports an error if updateDraft() throws', async () => {
    mockStoreOptions.actions.updateDraft.mockImplementationOnce(() => Promise.reject({}))
    ; (wrapper.vm as any).send()
    await flushPromises()
    // todo: mock reportError() from the lib
  })

  it('dispatches `cancelDraftSignature()` on [Cancel] click', async () => {
    // wrapper.find('*[data-test-id="cancel-button"]').vm.$emit('click')
    ; (wrapper.vm as any).cancel()
    await flushPromises()
    expect(mockStoreOptions.actions.cancelDraftSignature).toBeCalledTimes(1)
  })

  it('reports an error if `cancelDraftSignature()` throws', async () => {
    mockStoreOptions.actions.cancelDraftSignature.mockImplementationOnce(() => Promise.reject({}))
    ; (wrapper.vm as any).cancel()
    await flushPromises()
    // todo: mock reportError() from the lib
  })

  it('reports an error if `initNewMessage()` throws', async () => {
    let mockStoreOptions = createMockStoreOptions()
    mockStoreOptions.actions.initNewMessage.mockImplementationOnce(() => Promise.reject({ dd: 3 }))
    let wrapper = shallowMount(NewMessage, {
      ...createWrapperOpts(mockStoreOptions),
    })
    await flushPromises()
    // todo: mock reportError() from the lib
  })

  describe('having invalid form', () => {  // todo: more elegent?
    let mockStoreOptions = createMockStoreOptions()
    mockStoreOptions.state.newMessage.to = 'abc'
    let store = new Vuex.Store(mockStoreOptions)
    let wrapperOpts2 = {
      ...wrapperOpts,
      store,
    }
    let wrapper = shallowMount(NewMessage, wrapperOpts2)

    it('doesn’t dispatch actions with invalid form on send()', async () => {
      ; (wrapper.vm as any).send()
      await flushPromises()
      expect(mockStoreOptions.actions.updateDraft).not.toBeCalled()
    })

    it('doesn’t dispatch actions with invalid form on sign()', async () => {
      ; (wrapper.vm as any).sign()
      await flushPromises()
      expect(mockStoreOptions.actions.updateDraft).not.toBeCalled()
    })
  })
})

describe('LocaleDropdown.vue', () => {
  let wrapper = shallowMount(LocaleDropdown, {
    ...commonOptions,
    store,
  })

  it('has a disabled element', async () => {
    expect(wrapper.find('bdropdownitem-stub[disabled]').element).toBeTruthy()
  })

  it('changes the valu after a click', async () => {
    let firstLocale = store.state.settings.value.locale
    wrapper.find('bdropdownitem-stub[disabled]').trigger('click')
    await wrapper.vm.$nextTick()
    // wrapper.setProps({ side: 'left' })
    // expect(store.state.settings.value.locale).not.toBe(firstLocale)
  })
})

describe('TheHeader.vue', () => {
  let wrapper = shallowMount(TheHeader, {
    ...commonOptions,
    store,
  })
  it('mounts', async () => {
    expect(wrapper.isVueInstance()).toBeTruthy()
  })
})

describe('UserDropdown.vue', () => {
  let wrapper = shallowMount(UserDropdown, {
    ...commonOptions,
    router,
    store: new Vuex.Store({
      state: {
        me: new CachedValue({
          role: 'EE51001091072',
          id: 'EE51001091072',
          firstName: 'firstName',
          lastName: 'lastName',
        }),
      },
    }),
  })

  it('renders name', async () => {
    expect(wrapper.text()).toMatch('firstName')
  })
})

describe('ErrorLabel.vue', () => {
  let wrapper = shallowMount(ErrorLabel, {
    mocks: {
      ...i18nMock,
    },
    propsData: {
      error: {
        message: 'boo',
      },
    },
  })
  it('has html', async () => {
    expect(wrapper.find('veeralabel-stub').text()).toBe('serverError errors.api.unknown')
  })
})
