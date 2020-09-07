export function formatReceiver(receiver) {
  if (receiver.firstName && receiver.lastName) {
    return `${receiver.firstName} ${receiver.lastName} (${receiver.id})`
  }
  return receiver.id
}

export function formatSender(sender, $t) {
  let ret = `${sender.firstName} ${sender.lastName} (${sender.id})`
  if (sender.id === sender.roleId) {
    return ret
  }
  ret += ' ' + $t('onBehalfOf', { who: sender.roleId })
  return ret
}

export function bakeErrorMessage(errorCode, tPrefix, i18n, options) {
  errorCode = errorCode || 'unknown'
  let translationKey = `${tPrefix}.${errorCode}`
  let message = ''
  if (options.type === 'server') {
    message += i18n.t('serverError') + ' '
  }
  if (i18n.te(translationKey)) {
    message += i18n.t(translationKey)
  } else {
    message += `${i18n.t('Code')}: ${errorCode}.`
  }
  return message
}

export function reportError(errorCode, tPrefix, toast, i18n, options = { type: undefined }) {
  let title = i18n.t('Error')
  let message = bakeErrorMessage(errorCode, tPrefix, i18n, options)
  toast.toast(message, {
    title,
    autoHideDelay: 6000,
    variant: 'danger',
  })
}
