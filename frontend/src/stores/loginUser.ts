import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'
import { DEFAULT_USERNAME } from '@/constants/user'

/**
 * 登录用户信息
 */
export const useLoginUserStore = defineStore('loginUser', () => {

    userName: DEFAULT_USERNAME,
  })

  async function fetchLoginUser() {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
