import moment from 'moment'



export function formatEstonian(value: Date) {
  return moment(value).format('DD.MM.YYYY, HH:mm')
}
