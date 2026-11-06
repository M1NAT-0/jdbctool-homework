$(function () {
  function refreshCaptcha() {
    $('#captchaImg').attr('src', 'api/captcha?t=' + Date.now());
  }
  $('#captchaImg').on('click', refreshCaptcha);

  $('#resetBtn').on('click', function () {
    $('#name').val('');
    $('#password').val('');
    $('#captcha').val('');
    $('#tip').text('');
  });

  $('#submitBtn').on('click', async function () {
    const name = $('#name').val().trim();
    const password = $('#password').val();
    const captcha = $('#captcha').val().trim();
    if (!name || !password || !captcha) {
      $('#tip').text('请填写用户名、密码和验证码');
      return;
    }
    try {
      const res = await axios.post('api/login', { name: name, password: password, captcha: captcha });
      if (res.code === 0) {
        window.location.href = 'index.html';
      } else {
        $('#tip').text(res.message || '登录失败');
        refreshCaptcha();
      }
    } catch (e) {
      refreshCaptcha();
    }
  });
});
