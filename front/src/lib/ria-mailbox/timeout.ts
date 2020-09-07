export function timeout(msec: number) {
  return new Promise(resolve => setTimeout(resolve, msec))
}
