import * as componentHelpers from '@/lib/ria-mailbox/component-helpers'



describe('component-helpers.ts/formatReceiver', () => {
  it('returns id if receiver doesn’t have a name', async () => {
    expect(componentHelpers.formatReceiver({ id: 'foo' })).toBe('foo')
  })
})

describe('component-helpers.ts/reportError', () => {
  it('creates a toast', async () => {
    let toast = {
      toast: jest.fn(),
    }
    let i18n = {
      t: x => x,
      te: () => false,
    }
    componentHelpers.reportError(
      'fooCode',
      'fooPrefix',
      toast,
      i18n,
      { type: 'server' },
    )
    expect(toast.toast.mock.calls[0][0]).toBe('serverError Code: fooCode.')
  })
})
