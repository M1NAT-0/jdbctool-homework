const MENUS = {
  '管理员': [
    { key: 'userQuery', label: '用户信息查询' },
    { key: 'loanRecordQuery', label: '借阅记录查询' },
    { key: 'loanStats', label: '借阅数据统计' },
    { key: 'borrow', label: '借书' },
    { key: 'return', label: '还书' },
    { key: 'reviewRecommend', label: '审阅推荐图书' }
  ],
  '学生': [
    { key: 'borrow', label: '借书' },
    { key: 'return', label: '还书' },
    { key: 'myLoans', label: '个人借阅查询' }
  ],
  '操作员': [
    { key: 'addBook', label: '添加书籍' },
    { key: 'viewAllBooks', label: '查看所有书籍' },
    { key: 'discardBook', label: '废弃图书' },
    { key: 'editBook', label: '编辑图书' },
    { key: 'bookCopyEdit', label: '图书副本编辑' },
    { key: 'borrow', label: '借书' },
    { key: 'return', label: '还书' },
    { key: 'myLoans', label: '个人借阅查询' }
  ],
  '教师': [
    { key: 'borrow', label: '借书' },
    { key: 'return', label: '还书' },
    { key: 'myLoans', label: '个人借阅查询' },
    { key: 'recommend', label: '推荐图书' }
  ]
};

Vue.createApp({
  data() {
    return {
      me: null,
      currentView: null,
      userKeyword: '',
      users: null,
      showUserModal: false,
      userDetail: null,
      adminLoanMode: 'keyword',
      adminLoanKeyword: '',
      adminStart: '',
      adminEnd: '',
      adminLoans: null,
      typeStats: [],
      majorStats: [],
      books: [],
      bookKeyword: '',
      addForm: { title: '', author: '', type: '', publisher: '', days: null, price: null, stock: null, introduction: '' },
      editBookId: null,
      editForm: { title: '', author: '', type: '', publisher: '', days: null, price: null },
      copyBookId: null,
      copies: [],
      borrowable: [],
      myBorrowRecords: [],
      unreturned: [],
      myLoanMode: 'all',
      myLoanStart: '',
      myLoanEnd: '',
      myLoanList: [],
      recommendForm: { bookName: '', author: '', publisher: '', price: null },
      recommendResult: null,
      recommendMessage: '',
      pendingRecommends: []
    };
  },
  computed: {
    menu() {
      if (!this.me) return [];
      return MENUS[this.me.role] || [];
    }
  },
  async mounted() {
    try {
      const res = await axios.get('api/me');
      if (res.code === 0) {
        this.me = res.data;
        if (this.menu.length) {
          this.openView(this.menu[0].key);
        }
      } else {
        window.location.href = 'login.html';
      }
    } catch (e) {
      window.location.href = 'login.html';
    }
  },
  methods: {
    openView(key) {
      this.currentView = key;
      this.resetView(key);
    },
    resetView(key) {
      if (key === 'userQuery') { this.users = null; this.userKeyword = ''; }
      if (key === 'loanRecordQuery') { this.adminLoans = null; this.adminLoanKeyword = ''; }
      if (key === 'loanStats') { this.loadStats(); }
      if (key === 'addBook' || key === 'viewAllBooks' || key === 'discardBook' || key === 'editBook' || key === 'bookCopyEdit') {
        this.loadBooks();
        if (key === 'bookCopyEdit') this.copies = [];
      }
      if (key === 'borrow') { this.loadBorrowable(); this.loadMyBorrowRecords(); }
      if (key === 'return') { this.loadUnreturned(); }
      if (key === 'myLoans') { this.loadMyLoans(); }
      if (key === 'reviewRecommend') { this.loadPendingRecommends(); }
    },
    async logout() {
      try { await axios.post('api/logout'); } catch (e) {}
      window.location.href = 'login.html';
    },
    async queryUsers() {
      if (!this.userKeyword.trim()) { toast('请输入关键字', true); return; }
      const res = await axios.get('api/users', { params: { keyword: this.userKeyword.trim() } });
      if (res.code === 0) { this.users = res.data; } else { toast(res.message, true); }
    },
    showUser(u) { this.userDetail = u; this.showUserModal = true; },
    statusClass(l) {
      if (l.status === '已还') return 'returned';
      if (l.overdue) return 'overdue';
      return 'unreturned';
    },
    statusText(l) {
      if (l.status === '已还') return '已还';
      if (l.overdue) return '超期';
      return '未还';
    },
    async loadAdminLoans() {
      let params = { mode: this.adminLoanMode };
      if (this.adminLoanMode === 'range') {
        params.start = this.adminStart;
        params.end = this.adminEnd;
      } else {
        params.keyword = this.adminLoanKeyword;
      }
      const res = await axios.get('api/loans/admin', { params });
      if (res.code === 0) { this.adminLoans = res.data; } else { toast(res.message, true); }
    },
    async loadStats() {
      const r1 = await axios.get('api/stats/by-type');
      const r2 = await axios.get('api/stats/by-major');
      if (r1.code === 0) this.typeStats = r1.data;
      if (r2.code === 0) this.majorStats = r2.data;
    },
    async loadBooks() {
      const res = await axios.get('api/books', { params: { keyword: this.bookKeyword || '' } });
      if (res.code === 0) { this.books = res.data; } else { toast(res.message, true); }
    },
    async addBook() {
      const res = await axios.post('api/books', this.addForm);
      if (res.code === 0) {
        toast('添加成功');
        this.addForm = { title: '', author: '', type: '', publisher: '', days: null, price: null, stock: null, introduction: '' };
        this.bookKeyword = '';
        await this.loadBooks();
      } else {
        toast(res.message, true);
      }
    },
    async discardBook(b) {
      if (!confirm('确定废弃《' + b.title + '》吗？')) return;
      const res = await axios.post('api/books/' + b.id + '/discard');
      if (res.code === 0) { toast('已废弃'); await this.loadBooks(); } else { toast(res.message, true); }
    },
    startEditBook(b) {
      this.editBookId = b.id;
      this.editForm = { title: b.title, author: b.author, type: b.type, publisher: b.publisher, days: b.days, price: b.price };
    },
    async saveEditBook() {
      const res = await axios.put('api/books/' + this.editBookId, this.editForm);
      if (res.code === 0) { toast('保存成功'); await this.loadBooks(); } else { toast(res.message, true); }
    },
    async loadCopies() {
      if (!this.copyBookId) return;
      const res = await axios.get('api/books/' + this.copyBookId);
      if (res.code === 0 && res.data.bookCopies) {
        this.copies = res.data.bookCopies.map(c => ({ id: c.id, borrowed: c.isBorrowed }));
      } else {
        this.copies = [];
      }
    },
    async addCopy() {
      const res = await axios.post('api/bookcopies', null, { params: { bookId: this.copyBookId } });
      if (res.code === 0) { toast('新增副本成功'); await this.loadCopies(); await this.loadBooks(); } else { toast(res.message, true); }
    },
    async deleteCopy(c) {
      if (!confirm('确定删除该副本吗？')) return;
      const res = await axios.delete('api/bookcopies/' + c.id);
      if (res.code === 0) { toast('删除成功'); await this.loadCopies(); await this.loadBooks(); } else { toast(res.message, true); }
    },
    async loadBorrowable() {
      const res = await axios.get('api/loans/borrowable');
      if (res.code === 0) { this.borrowable = res.data; } else { toast(res.message, true); }
    },
    async loadMyBorrowRecords() {
      const res = await axios.get('api/loans/mine', { params: { mode: 'all' } });
      if (res.code === 0) { this.myBorrowRecords = res.data; } else { toast(res.message, true); }
    },
    async doBorrow(b) {
      const res = await axios.post('api/loans/borrow', { bookId: b.id });
      if (res.code === 0) { toast('借阅成功'); await this.loadBorrowable(); await this.loadMyBorrowRecords(); } else { toast(res.message, true); }
    },
    async loadUnreturned() {
      const res = await axios.get('api/loans/mine', { params: { mode: 'unreturned' } });
      if (res.code === 0) { this.unreturned = res.data; } else { toast(res.message, true); }
    },
    async doReturn(l) {
      const res = await axios.post('api/loans/return', { bookCopyId: l.bookCopyId });
      if (res.code === 0) { toast('还书成功'); await this.loadUnreturned(); } else { toast(res.message, true); }
    },
    async loadMyLoans() {
      let params = { mode: this.myLoanMode };
      if (this.myLoanMode === 'range') {
        params.start = this.myLoanStart;
        params.end = this.myLoanEnd;
      }
      const res = await axios.get('api/loans/mine', { params });
      if (res.code === 0) { this.myLoanList = res.data; } else { toast(res.message, true); }
    },
    async submitRecommend() {
      if (!this.recommendForm.bookName) { toast('请输入书名', true); return; }
      const res = await axios.post('api/recommend', this.recommendForm);
      if (res.code === 0) {
        this.recommendResult = res.data;
        this.recommendMessage = res.data.message || '';
      } else { toast(res.message, true); }
    },
    async loadPendingRecommends() {
      const res = await axios.get('api/recommend');
      if (res.code === 0) { this.pendingRecommends = res.data; } else { toast(res.message, true); }
    },
    async approveRecommend(r) {
      const res = await axios.post('api/recommend/' + r.id + '/approve');
      if (res.code === 0) { toast('已加入图书列表'); await this.loadPendingRecommends(); } else { toast(res.message, true); }
    },
    async rejectRecommend(r) {
      if (!r.rejectReason) { toast('请填写驳回理由', true); return; }
      const res = await axios.post('api/recommend/' + r.id + '/reject', { reason: r.rejectReason });
      if (res.code === 0) { toast('已驳回'); await this.loadPendingRecommends(); } else { toast(res.message, true); }
    }
  }
}).mount('#app');
