/* 머니로그 공통 스크립트
 * - 모달(dialog) 열기/닫기
 * - 금액 빠른 입력 칩
 * - file:// 로 직접 열었을 때만 링크/폼을 목업 html 간 이동으로 치환 (서버에서는 동작하지 않음)
 */
(function () {
  /* 모달 */
  document.addEventListener('click', function (e) {
    var open = e.target.closest('[data-dialog]');
    if (open) {
      var dlg = document.getElementById(open.dataset.dialog);
      if (!dlg) return;
      var form = dlg.querySelector('form');
      if (open.dataset.action && form) form.setAttribute('action', open.dataset.action);
      dlg.querySelectorAll('[data-fill]').forEach(function (el) {
        var v = open.dataset[el.dataset.fill];
        if (v) el.textContent = v;
      });
      dlg.showModal();
      return;
    }
    if (e.target.closest('[data-close]')) {
      e.target.closest('dialog').close();
      return;
    }
    if (e.target.tagName === 'DIALOG') e.target.close(); // 배경 클릭

    /* 금액 빠른 입력 */
    var add = e.target.closest('[data-add]');
    if (add) {
      var input = document.querySelector(add.dataset.target);
      if (!input) return;
      var cur = Number(input.value || 0);
      input.value = add.dataset.add === 'all' ? add.dataset.all : cur + Number(add.dataset.add);
      input.dispatchEvent(new Event('input', { bubbles: true }));
    }
  });

  /* 이체 요약 실시간 반영 */
  var amt = document.getElementById('amount');
  var sumAmt = document.getElementById('sum-amount');
  if (amt && sumAmt) {
    amt.addEventListener('input', function () {
      var n = Number(amt.value || 0);
      sumAmt.textContent = n.toLocaleString('ko-KR') + '원';
      var t = document.getElementById('sum-total');
      if (t) t.textContent = n.toLocaleString('ko-KR') + '원';
    });
  }

  /* 삭제 확인 */
  document.addEventListener('submit', function (e) {
    var c = e.target.dataset.confirm;
    if (c && !window.confirm(c)) e.preventDefault();
  }, true);

  /* ===== 미리보기 모드 (file://) ===== */
  if (location.protocol !== 'file:') return;
  function toFile(p) {
    p = p.split('?')[0];
    if (p === '/accounts/signup') return 'signup.html';
    if (p === '/accounts/login') return 'login.html';
    if (p === '/banking/accounts') return 'accounts.html';
    if (p === '/banking/accounts/new') return 'account-form.html';
    if (/^\/banking\/accounts\/\d+$/.test(p)) return 'account-detail.html';
    if (p === '/banking/transfer') return 'transfer.html';
    if (p === '/expenses') return 'expenses.html';
    if (p === '/expenses/new') return 'expense-form.html';
    if (p === '/goals') return 'goals.html';
    if (p === '/goals/new') return 'goal-form.html';
    return p;
  }
  document.querySelectorAll('a[href^="/"]').forEach(function (a) {
    a.setAttribute('href', toFile(a.getAttribute('href')));
  });
  document.querySelectorAll('form').forEach(function (f) {
    f.addEventListener('submit', function (e) {
      e.preventDefault();
      if (f.dataset.next) location.href = f.dataset.next;
    });
  });
})();
