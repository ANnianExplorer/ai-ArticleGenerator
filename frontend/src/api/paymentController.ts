
/* eslint-disable */
import request from '@/request'

/** 创建 VIP 支付会话 POST /payment/create-vip-session */
export async function createVipPaymentSession(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/payment/create-vip-session', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 获取当前用户支付记录 GET /payment/records */
export async function getPaymentRecords(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPaymentRecord>('/payment/records', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 申请退�?POST /payment/refund */
export async function refund(

  params: API.refundParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/payment/refund', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
