<template>
  <BOverlay
    :show="!$store.state.newMessageInitedFromServer"
    class="overlay-default"
  >
    <div class="container">
      <BForm
        class="form"
        novalidate
        @submit.prevent
      >
        <BFormGroup
          :disabled="disabled"
          :label="$t('To')"
          label-cols-sm="auto"
          label-align="left"
          label-align-sm="right"
        >
          <BFormInput
            v-model="to"
            v-mask="'###########'"
            :placeholder="$t('receiverPlaceholder')"
            :state="validationStateTo"
            required
            minlength="1"
            maxlength="11"
            pattern="\d{1,11}"
            inputmode="numeric"
            autofocus
            @input="onInput"
          />
          <BFormInvalidFeedback :state="validationStateTo">
            <i class="material-icons-outlined icon">error_outline</i>
            <span class="message-type">{{ $t('Error message') }}:</span>
            {{ $t('Empty receiver code') }}!
          </BFormInvalidFeedback>
        </BFormGroup>
        <BFormGroup
          :label="$t('Subject')"
          :disabled="disabled"
          label-cols-sm="auto"
          label-align="left"
          label-align-sm="right"
        >
          <BFormInput
            v-model="subject"
            class="subject"
            maxlength="255"
            autocomplete="off"
            @input="onInput"
          />
        </BFormGroup>
        <BFormGroup
          :label="$t('Text')"
          :disabled="disabled"
          label-cols-sm="auto"
          label-align="left"
          label-align-sm="right"
        >
          <BFormTextarea
            v-model="body"
            :state="null"
            maxlength="2048"
            autocomplete="off"
            rows="4"
            @input="onInput"
          />
          <div
            v-if="signature"
            class="signed-by"
          >
            <SignedBy
              :signature="$store.state.newMessage.signature"
              message-id="draft"
            />
          </div>
        </BFormGroup>
        <BFormGroup
          label
          :disabled="disabled"
          label-cols-sm="auto"
          label-align="left"
          label-align-sm="right"
          class="text-left"
        >
          <FileSelector
            :disabled="$store.getters.hasReachedNumFilesLimit"
            multiple
            @input="addAttachments"
          />
          <BFormInvalidFeedback :state="!$store.getters.hasReachedNumFilesLimit">
            <i class="material-icons-outlined icon">error_outline</i>
            {{ $t('numAttachmentsLimitReachedError', { max: $store.state.draftSettings.maxAttachmentNumber }) }}
          </BFormInvalidFeedback>
        </BFormGroup>
        <BFormGroup
          v-if="attachmentList.length"
          label
          label-cols-sm="auto"
          label-align="left"
          label-align-sm="right"
        >
          <FileUploadList
            class="attachment-list"
            :files="attachmentList"
            :disabled="disabled"
          />
        </BFormGroup>
        <BFormGroup
          class="button"
          label-cols-sm="auto"
          label-align="left"
          label-align-sm="right"
        >
          <div
            v-if="signature"
            class="cancel-and-send-buttons mt-4"
          >
            <VeeraButton
              variant="secondary"
              :disabled="['sending'].includes(status)"
              :busy="status==='cancelling'"
              data-test-id="cancel-button"
              @click="cancel"
            >{{ $t('Cancel') }}</VeeraButton>
            <VeeraButton
              variant="primary"
              mat-icon="send"
              :disabled="['cancelling'].includes(status)"
              :busy="status==='sending'"
              @click="dispatch"
            >{{ $t('Send message') }}</VeeraButton>
          </div>
          <div
            v-else
            class="sign-and-send-buttons mt-4"
          >
            <VeeraButton
              variant="secondary"
              :busy="status==='signing'"
              :disabled="['sending'].includes(status) || $store.getters.isBusyWithFiles"
              class="sign-button"
              @click="sign"
            >
              <template v-slot:icon>
                <IdCardIcon
                  class="sign-icon"
                  color="black"
                />
              </template>
              {{ $t('signWithIdCard') }}
            </VeeraButton>
            <VeeraButton
              variant="primary"
              :busy="status==='sending'"
              :disabled="['signing'].includes(status) || $store.getters.isBusyWithFiles"
              data-test-id="send-button"
              @click="send"
            >{{ $t('Send') }}</VeeraButton>
          </div>
        </BFormGroup>
      </BForm>
    </div>
  </BOverlay>
</template>

<script lang="ts">
import Vue from 'vue'
import SignedBy from './SignedBy.vue'
import FileUploadList from './FileUploadList.vue'
import {
  BForm,
  BFormGroup,
  BFormInput,
  BFormInvalidFeedback,
  BFormTextarea,
  BOverlay,
} from 'bootstrap-vue'
import IdCardIcon from '@/lib/vue/icons/IdCardIcon.vue'
import VeeraButton from 'veera-vue/vue/VeeraButton.vue'
import FileSelector from 'veera-vue/vue/FileSelector.vue'
import { reportError } from '@/lib/ria-mailbox/component-helpers'
import { mask } from 'vue-the-mask'
import { hwcrypto } from '@/lib/hwcrypto'
import throttle from 'lodash/throttle'

import { mapFields } from 'vuex-map-fields'

export default Vue.extend({
  components: {
    BForm,
    BFormGroup,
    BFormInput,
    BFormInvalidFeedback,
    BFormTextarea,
    IdCardIcon,
    VeeraButton,
    SignedBy,
    FileSelector,
    FileUploadList,
    BOverlay,
  },
  directives: {
    mask,
  },
  data() {
    return {
      status: undefined,
    }
  },
  computed: {
    ...mapFields([
      'newMessage.signature',
      'newMessage.to',
      'newMessage.subject',
      'newMessage.body',
      'newMessage.validationTriggerred',  // todo: not a field?
    ]),
    disabled() {
      return !!(this.status || this.signature)
    },
    validationStateTo() {
      if (!this.validationTriggerred) {
        return null
      }
      return /^\d{1,11}$/.test(this.to)
    },
    attachmentList() {
      return this.$store.state.newMessage.attachments.map((x, i) => ({
        ...x,
        object: x,  // todo
        numFilesLimitStatus: this.$store.getters.numFilesLimitStatuses.statuses[i],
      }))
    },
  },
  async created() {
    // todo: test
    // window.addEventListener('unload', this.onInput.flush)
    try {
      await this.$store.dispatch('initNewMessage')
    } catch (e) {
      this.reportError(e?.response?.data?.error, 'errors.api')
    }
  },
  methods: {
    onInput: throttle(async function() {
      try {
        await this.$store.dispatch('updateDraft')
      } catch (e) {
        this.reportError(e?.response?.data?.error, 'errors.api')
      }
    }, 1000, { leading: false }),
    async sign() {
      this.validationTriggerred = true
      if (!this.validationStateTo) {
        return
      }
      this.status = 'signing'
      // await this.onInput.flush()  // todo
      try {
        await this.$store.dispatch('updateDraft')
        await this.$store.dispatch('signDraft')
      } catch (e) {
        // todo: more elegent
        let isHwcrypto = [
          hwcrypto.NO_IMPLEMENTATION,
          hwcrypto.USER_CANCEL,
          hwcrypto.NOT_ALLOWED,
          hwcrypto.NO_CERTIFICATES,
          hwcrypto.TECHNICAL_ERROR,
          hwcrypto.INVALID_ARGUMENT,
        ].includes(e?.message)
        if (isHwcrypto) {
          if (e?.message === hwcrypto.USER_CANCEL) {
            // do nothing
          } else {
            this.reportError(e?.message, 'errors.hwcrypto')
          }
        } else {
          this.reportError(e?.response?.data?.error, 'errors.api')
        }
      } finally {
        this.status = undefined
      }
    },
    async send() {
      this.validationTriggerred = true
      if (!this.validationStateTo) {
        return
      }
      this.status = 'sending'
      try {
        await this.$store.dispatch('updateDraft')
        await this.dispatch()
      } catch (e) {
        // todo
        this.reportError(e.response?.data?.error, 'errors.api')
      } finally {
        this.status = undefined
      }
    },
    async dispatch() {
      this.status = 'sending'
      try {
        await this.$store.dispatch('dispatchDraft')
        this.$router.push('out')
      } catch (e) {
        this.reportError(e.response?.data?.error, 'errors.api')
      } finally {
        this.status = undefined
      }
    },
    async cancel() {
      this.status = 'cancelling'
      try {
        await this.$store.dispatch('cancelDraftSignature')
      } catch (e) {
        this.reportError(e.response?.data?.error, 'errors.api')
      } finally {
        this.status = undefined
      }
    },
    reportError(code: string, tPrefix: string) {
      reportError(code, tPrefix, this.$bvToast, this.$i18n, {
        type: tPrefix === 'errors.hwcrypto' ? 'crypto' : 'server',
      })
    },
    addAttachments(files) {
      this.$store.dispatch('addAttachments', [...files])
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.container {
  padding: $tab-padding;
  display: flex;
  justify-content: center;

  .form {
    flex-basis: 30em;

    ::v-deep {
      .input-group,
      .subject,
      textarea {
        max-width: 30em;
      }
      legend {
        min-width: 4em;
      }
    }
    .button {
      text-align: center;
    }
  }
}

.icon {
  font-size: 1.2em;
  vertical-align: middle;
  line-height: inherit;
}

// todo: move to veera component lib?
.invalid-feedback {
  $background-color: getColor(jasper-3);

  margin-top: 0.4em;
  padding: 0.3em 0.6em 0.2em;
  background-color: $background-color;
  color: getColor(black-coral-20);
  box-shadow: 0px 1px 5px rgba(0, 0, 0, 0.2);
  border-radius: 4px;

  &:before {
    $size: 10px;

    content: "";
    width: 0;
    height: 0;
    border-left: $size solid transparent;
    border-right: $size solid transparent;
    border-bottom: $size solid $background-color;
    position: absolute;
    margin-top: -$size;
  }

  .message-type {
    margin-left: 0.5em;
    font-weight: bold;
  }
}

.cancel-and-send-buttons {
  display: flex;
  justify-content: space-evenly;
  flex-wrap: wrap;
  align-content: space-between;
  @media screen and (max-width: 420px) {
    flex-direction: column;
    align-content: center;
    :nth-child(2) {
      margin-top: 1em;
    }
  }
}
.sign-and-send-buttons {
  // todo: reuse
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  align-content: space-between;
  @media screen and (max-width: 420px) {
    flex-direction: column;
    align-content: center;
    :nth-child(2) {
      margin-top: 1em;
    }
  }
}

.attachment-list {
  font-size: 0.8em;
}

.signed-by {
  margin-top: 1em;
  font-size: $o-font-size-sm;
  text-align: right;
}

.sign-icon {
  margin-right: 0.4em;
}
.num-attachments-error {
  font-size: 0.8em;
  color: $color-error;
}
</style>
