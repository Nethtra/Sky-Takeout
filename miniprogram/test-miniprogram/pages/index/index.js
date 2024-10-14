// index.js
Page({
  data: {
    msg: 'hello world',
    nickName: '',
    url: '',
    code: '' //授权码
  },
  //获取用户信息
  getUserInfo() {
    //使用wx的方法
    wx.getUserProfile({
      desc: '获取当前用户信息', //描述
      success: (res) => { //结果
        console.log(res.userInfo);
        this.setData({
          nickName: res.userInfo.nickName, //昵称
          url: res.userInfo.avatarUrl //头像
        })
      }
    })
  },
  //微信登陆获取授权码 
  //每次获取都会生成新的授权码
  wxLogin() {
    wx.login({
      success: (res) => {
        console.log(res.code);
        this.setData({
          code: res.code
        })
      },
    })
  },
  //发送请求  要先启动后端
  sendRequest() {
    wx.request({
      url: 'http://localhost:8080/user/shop/status',
      method: 'GET',
      success: (res) => {
        console.log(res.data);//res.data代表响应的整个数据
      }
    })
  }
})