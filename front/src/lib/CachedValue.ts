export class CachedValue<T = any> {
  // todo: develop naming conventions for private/getter collision
  constructor(
    private _value: T = undefined,
    private isValid = false,
  ) {
  }

  get value() {
    return this._value
  }

  set(value: T) {
    this._value = value
    this.isValid = true
  }

  get valid() {
    return this.isValid
  }

  invalidate() {
    this.isValid = false
  }
}
