import axios from 'axios'
import { message } from 'ant-design-vue'
import { API_BASE_URL } from '@/config/env'
import { REQUEST_TIMEOUT, UNAUTHORIZED_CODE } from '@/constants'


const myAxios = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  withCredentials: true,
})


  function (config) {

    return config
  },
  function (error) {

    return Promise.reject(error)
  },
)


  function (response) {
    const { data } = response


      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {


    return Promise.reject(error)
  },
)

export default myAxios
