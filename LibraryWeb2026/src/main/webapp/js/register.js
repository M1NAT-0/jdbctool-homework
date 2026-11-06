$(function () {
  const fieldsMap = {
    '学生': '#studentFields',
    '教师': '#teacherFields',
    '操作员': '#operatorFields',
    '管理员': '#adminFields'
  };

  function switchRoleFields() {
    const role = $('#role').val();
    $.each(fieldsMap, function (key, selector) {
      $(selector).toggle(key === role);
    });
  }
  $('#role').on('change', switchRoleFields);
  switchRoleFields();

  $('#resetBtn').on('click', function () {
    $('.auth-box input').val('');
    $('#role').val('学生');
    switchRoleFields();
    $('#tip').text('');
  });

  $('#submitBtn').on('click', async function () {
    const role = $('#role').val();
    const payload = {
      name: $('#name').val().trim(),
      password: $('#password').val(),
      phone: $('#phone').val().trim(),
      major: $('#major').val().trim(),
      role: role
    };
    if (role === '学生') {
      payload.grade = $('#grade').val().trim();
      payload.college = $('#college').val().trim();
    } else if (role === '教师') {
      payload.college = $('#tCollege').val().trim();
      payload.title = $('#title').val().trim();
    } else if (role === '操作员') {
      payload.duty = $('#duty').val().trim();
      payload.area = $('#area').val().trim();
    } else if (role === '管理员') {
      payload.permissionLevel = parseInt($('#permissionLevel').val() || '1', 10);
      payload.entryTime = $('#entryTime').val();
    }
    try {
      const res = await axios.post('api/register', payload);
      if (res.code === 0) {
        toast('注册成功，请登录');
        setTimeout(function () { window.location.href = 'login.html'; }, 900);
      } else {
        $('#tip').text(res.message || '注册失败');
      }
    } catch (e) {
      $('#tip').text('注册失败，请稍后重试');
    }
  });
});
