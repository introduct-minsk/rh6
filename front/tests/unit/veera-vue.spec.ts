/* eslint-disable no-lone-blocks */

import { mount, shallowMount, createLocalVue } from '@vue/test-utils'

import StateServiceLogo from 'veera-vue/vue/StateServiceLogo.vue'
import MenuDropdown from 'veera-vue/vue/MenuDropdown.vue'
import FileSelector from 'veera-vue/vue/FileSelector.vue'
import FileSelectorButton from 'veera-vue/vue/FileSelectorButton.vue'
import VeeraButton from 'veera-vue/vue/VeeraButton.vue'
import SideNav from 'veera-vue/vue/SideNav.vue'

import BootstrapVue from 'bootstrap-vue'



const localVue = createLocalVue()
localVue.use(BootstrapVue)

let commonMocks = {
  $t: x => x,
}

describe('StateServiceLogo.vue', () => {
  {
    let wrapper = shallowMount(StateServiceLogo, {
      propsData: {
        lines: [
          'line1',
          'line2',
        ],
      },
    })
    it('renders an svg', async () => {
      expect(wrapper.find('svg').element).toBeTruthy()
    })
    it('renders two lines of text', async () => {
      expect(wrapper.find('div.text').findAll('p').length).toBe(2)
    })
    it('the line 2 has correct content', async () => {
      expect(wrapper.find('p.line2').text()).toBe('line2')
    })
  }
  {
    let wrapper = shallowMount(StateServiceLogo, {
    })
    it('by default has emty lines', async () => {
      expect(wrapper.find('p.line1').text()).toBe('')
    })
  }
})

describe('FileSelectorButton.vue', () => {
  let wrapper = shallowMount(FileSelectorButton)

  it('passes buttonClass to the button', async () => {
    await wrapper.setProps({ buttonClass: 'foo' })
    expect(wrapper.find('button').classes('foo')).toBeTruthy()
    await wrapper.setProps({ buttonClass: 'bar' })
    expect(wrapper.find('button').classes('foo')).toBeFalsy()
    expect(wrapper.find('button').classes('bar')).toBeTruthy()
  })

  it('passes the multiple prop to the input', async () => {
    await wrapper.setProps({ multiple: 'multiple' })
    expect(wrapper.find('input').attributes('multiple')).toBeTruthy()
    await wrapper.setProps({ multiple: false })
    expect(wrapper.find('input').attributes('multiple')).toBeFalsy()
  })

  // clicks cannot be covered because of jest’s limitation:
  // https://stackoverflow.com/a/49145274
  // https://github.com/jsdom/jsdom/issues/1568
  // we therefore call the methods directly
  it('emits an input event only on non-empty file input', async () => {
    { (wrapper.vm as any).clicked() }
    { (wrapper.vm as any).onSelected([]) }
    expect(wrapper.emitted('input')).toBeFalsy()
    { (wrapper.vm as any).onSelected([{}]) }
    expect(wrapper.emitted('input')).toBeTruthy()
  })
})

describe('FileSelector.vue', () => {
  let wrapper = mount(FileSelector, {
    mocks: {
      ...commonMocks,
    },
  })

  it('mounts', async () => {
    expect(wrapper.isVueInstance()).toBeTruthy()
  })

  it('sets/removes .drop-hovered on hover/leave', async () => {
    expect(wrapper.find('.drop-hovered').element).toBeFalsy()
    await wrapper.find('.dropzone').trigger('dragenter')
    expect(wrapper.find('.drop-hovered').element).toBeTruthy()
    await wrapper.find('.drop-mask').trigger('dragleave')
    expect(wrapper.find('.drop-hovered').element).toBeFalsy()
  })

  // clicks cannot be covered because of jest’s limitation:
  // https://stackoverflow.com/a/49145274
  // https://github.com/jsdom/jsdom/issues/1568
  // we therefore call the methods directly

  it('emits ‘input’ onInput', async () => {
    { (wrapper.vm as any).onInput() }
    expect(wrapper.emitted('input').length).toBe(1)
  })

  it('emits ‘input’ on drop', async () => {
    { (wrapper.vm as any).onDrop({ dataTransfer: { items: [{ kind: 'file', getAsFile: () => ({}) }] } }) }
    expect(wrapper.emitted('input').length).toBe(2)
  })
})

describe('SideNav.vue', () => {
  let items = [
    {
      name: 'item1',
      url: 'item1-url',
    },
  ]
  let mocks = {
    ...commonMocks,
    $route: {
      path: items[0].url,
    },
  }
  const createWrapper = () => shallowMount(SideNav, {
    propsData: {
      windowWidth: 1600,
    },
    mocks,
  })
  let wrapper = createWrapper()

  it('renders the last item', async () => {
    await wrapper.setProps({ items, windowWidth: 1600 })
    expect(wrapper.find('div.item:last-of-type span.title').text()).toBe(items[0].name)
  })

  it('hides on burger click', async () => {
    await wrapper.find('div.item').trigger('click')
    expect(wrapper.classes('hidden')).toBeTruthy()
  })

  it('has Collapse clickable when wide', () => {
    expect(wrapper.find(':first-child > div.item.clickable').element).toBeTruthy()
  })

  it('has Collapse unclickable when narrow', async () => {
    await wrapper.setProps({ items: { ...items }, windowWidth: 400 })
    expect(wrapper.find(':first-child > div.item.clickable').element).toBeFalsy()
  })

  it('emits if ignore-screen-width set', async () => {
    wrapper.setProps({ windowWidth: 1000, ignoreScreenWidth: true })
    await wrapper.find('div.item').trigger('click')
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  describe('when narrow', () => {
    let wrapper = createWrapper()
    wrapper.setProps({ windowWidth: 200 })
    it('hides on burger click', async () => {
      await wrapper.find('div.item').trigger('click')
      expect(wrapper.classes('hidden')).toBeTruthy()
    })
  })
})

describe('MenuDropdown.vue', () => {
  const wrapper = mount(MenuDropdown, {
    localVue,
    propsData: {
      label: 'A Label',
      value: 'value',
      side: 'right',
    },
    slots: {
      default: '<div class="content">dropdown content</div>',
    },
  })

  it('renders content when clicked', async () => {
    await wrapper.find('button').trigger('click')
    expect(wrapper.find('.content').isVisible()).toBeTruthy()
  })
})

describe('VeeraButton.vue', () => {
  let createWrapper = () => shallowMount(VeeraButton, {
    propsData: {
      variant: 'primary',
    },
  })
  let wrapper = createWrapper()

  it('renders an icon from the props', async () => {
    await wrapper.setProps({ matIcon: 'fooIcon' })
    expect(wrapper.find('.material-icons-outlined').text()).toBe('fooIcon')
  })

  it('relays the button click event', async () => {
    wrapper.find('button').trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })

  it('shows a spinner when busy', async () => {
    expect(wrapper.find('bspinner-stub').element).toBeFalsy()
    await wrapper.setProps({ busy: true })
    expect(wrapper.find('bspinner-stub').element).toBeTruthy()
  })

  {
    let wrapper = createWrapper()
    it('doesn’t emit click when busy', async () => {
      await wrapper.setProps({ busy: true })
      wrapper.find('button').trigger('click')
      expect(wrapper.emitted('click')).toBeFalsy()
    })
  }
})
