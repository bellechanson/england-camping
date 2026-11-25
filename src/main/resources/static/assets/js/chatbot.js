/*
 * 잉글랜드 캠핑장 AI 챗봇 "영국이" (손님 화면 오른쪽 아래)
 * - 질문 → POST /api/chat → 답을 말풍선으로
 * - 대화는 이 탭에서만 기억 (sessionStorage: 탭을 닫으면 사라짐). 서버에는 저장하지 않음
 *   → 다른 페이지로 넘어가도 대화가 이어서 보임
 * - 답은 textContent 로만 넣음 (HTML로 해석하지 않음 → 이상한 글이 와도 화면이 깨지거나 스크립트가 실행되지 않음)
 * - 영국이 버튼: 누르면 창 열기 / 잡아서 끌거나 던지면 → 쿵 떨어져서 바닥을 걸어 제자리로
 * - 관리자가 끄면 (#chatbot.is-sleeping) 영국이는 자는 모습 + Zzz.
 *   눌러도 "지금은 이용할 수 없어요" 안내만 보이고 질문 칸이 없음. 잡고 던지기는 그대로 됨
 */
(() => {
    const root = document.getElementById('chatbot');
    if (!root) {
        return; // 챗봇이 꺼진 페이지
    }

    const launcher = document.getElementById('chatLauncher');
    const panel = document.getElementById('chatPanel');
    const closeBtn = document.getElementById('chatClose');
    const body = document.getElementById('chatBody');
    const chips = document.getElementById('chatChips');
    const form = document.getElementById('chatForm');
    const input = document.getElementById('chatInput');
    const remainEl = document.getElementById('chatRemain');
    const phone = root.dataset.phone || '';
    const sleeping = root.classList.contains('is-sleeping'); // 관리자가 끈 상태 (자는 중)

    const STORE_KEY = 'englandChat'; // sessionStorage 이름
    const HISTORY_SEND = 6;          // 서버로 보낼 앞 대화 수 (application.properties 의 chatbot.history-size 와 같게)
    const REMAIN_SHOW = 5;           // 남은 질문이 이 이하일 때만 표시
    const canHover = window.matchMedia('(hover: hover)').matches; // PC(마우스)인지 → 열 때 입력 칸에 바로 커서

    let history = []; // [{role: 'user' | 'model', text: '...'}]
    let busy = false; // 답을 기다리는 중이면 true (중복 전송 막기)

    // ===== 말풍선 하나 추가 =====
    function addBubble(kind, text) {
        const div = document.createElement('div');
        div.className = 'chat-msg ' + kind;
        div.textContent = text;
        body.appendChild(div);
        body.scrollTop = body.scrollHeight; // 맨 아래로
        return div;
    }

    // ===== 대화 저장 (실패해도 챗봇은 동작) =====
    function save() {
        try {
            sessionStorage.setItem(STORE_KEY, JSON.stringify(history.slice(-20)));
        } catch (e) {
            // 저장 못 해도 괜찮음 (시크릿 모드 등)
        }
    }

    // ===== 열기 / 닫기 =====
    function open() {
        panel.hidden = false;
        root.classList.add('is-open');
        launcher.setAttribute('aria-expanded', 'true');
        body.scrollTop = body.scrollHeight;
        if (canHover && input) {
            input.focus(); // 폰에서는 키보드가 갑자기 올라오지 않게 PC만 (자는 중이면 입력 칸 없음)
        }
    }

    function close() {
        panel.hidden = true;
        root.classList.remove('is-open');
        launcher.setAttribute('aria-expanded', 'false');
        launcher.focus();
    }

    // ===== 영국이 잡아서 끌기 → 놓으면 쿵 떨어져서 바닥을 걸어 제자리로 =====
    // pointer 이벤트 = 마우스·손가락·펜을 한 번에 다루는 방식
    // - 누른 채 조금(6px) 넘게 움직이면 "끌기", 그보다 적게 움직이고 떼면 "누르기"(창 열기)
    // - 놓으면 (던지든 그냥 놓든) 중력으로 떨어짐 → 화면 맨 아래 바닥에 쿵(살짝 납작)
    //   → 잠깐 쉬었다가 바닥을 따라 뒤뚱뒤뚱 걸어서 → 제자리로 한 걸음 올라감
    const DRAG_START = 6;       // 이만큼(px) 넘게 움직이면 끌기
    const RETURN_SPEED = 400;   // 바닥을 걸어오는 속도 (1초에 몇 px). 작을수록 느림
    const GRAVITY = 0.004;      // 중력 (클수록 빨리 떨어짐. 1ms마다 아래로 빨라지는 양)
    const AIR = 0.985;          // 공기 저항: 순간마다 남는 옆 빠르기 비율 (작을수록 옆으로 덜 날아감)
    const MAX_THROW = 3;        // 던지는 빠르기 상한 (1ms에 px) — 너무 세게 날아가지 않게
    const REST_MS = 350;        // 바닥에 떨어진 뒤 쉬는 시간 (ms)

    let drag = null;            // 끄는 중 정보
    let offset = { x: 0, y: 0 };  // 지금 제자리에서 얼마나 떨어져 있는지 (px)
    let home = null;            // 제자리의 화면 위치 (바닥·벽 계산용)
    let flyFrame = 0;           // 떨어지는 애니메이션 번호 (멈출 때 씀)
    let restTimer = 0;          // 쉬기·걷기 타이머
    let suppressClick = false;  // 끌고 놓을 때 따라오는 클릭은 무시 (창이 열리지 않게)

    // 영국이를 제자리에서 (x, y)만큼 떨어진 곳에 그림
    function moveTo(x, y) {
        offset = { x: x, y: y };
        launcher.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
    }

    // 하던 움직임(떨어지기·쉬기·걷기)을 멈추고, 지금 눈에 보이는 자리에 고정
    function stopMotion() {
        cancelAnimationFrame(flyFrame);
        clearTimeout(restTimer);
        launcher.classList.remove('is-landing', 'is-walking');
        const m = new DOMMatrixReadOnly(getComputedStyle(launcher).transform);
        launcher.style.transition = 'none';
        moveTo(m.m41, m.m42); // m41, m42 = 지금 실제로 옮겨져 있는 x, y
    }

    // 떨어지기: (vx, vy) 빠르기로 출발 → 중력으로 아래로 점점 빨라짐 → 바닥에 닿으면 land()
    function fall(vx, vy) {
        let last = performance.now();
        launcher.style.transition = 'none';

        function step(now) {
            const dt = Math.min(now - last, 32); // 지난 그림 이후 흐른 시간 (ms, 너무 크면 32로)
            last = now;
            vy += GRAVITY * dt; // 중력: 아래로 점점 빨라짐
            let x = offset.x + vx * dt;
            let y = offset.y + vy * dt;

            // 벽·천장: 닿으면 그 방향 빠르기 0 (튕기지 않음)
            const minX = -home.left;
            const maxX = window.innerWidth - home.right;
            const minY = -home.top;
            const floor = window.innerHeight - home.bottom; // 바닥 = 영국이 발이 화면 맨 아래에 닿는 곳
            if (x < minX || x > maxX) {
                x = Math.min(Math.max(x, minX), maxX);
                vx = 0;
            }
            if (y < minY) {
                y = minY;
                vy = 0;
            }
            if (y >= floor) {
                moveTo(x, floor);
                land(); // 쿵!
                return;
            }
            moveTo(x, y);
            vx *= Math.pow(AIR, dt / 16); // 옆으로 가는 빠르기는 공기 저항으로 조금씩 줄어듦
            flyFrame = requestAnimationFrame(step);
        }
        flyFrame = requestAnimationFrame(step);
    }

    // 바닥에 쿵: 살짝 납작해졌다 펴짐(CSS is-landing) → 잠깐 쉬었다가 걸어오기
    function land() {
        launcher.classList.remove('is-dragging');
        launcher.classList.add('is-landing');
        restTimer = setTimeout(() => {
            launcher.classList.remove('is-landing');
            walkHome();
        }, REST_MS);
    }

    // 바닥을 따라 같은 속도로 뒤뚱뒤뚱 걸어서(CSS is-walking) 제자리 밑까지 → 한 걸음 올라가 제자리
    function walkHome() {
        launcher.classList.add('is-walking');
        const seconds = Math.max(0.2, Math.abs(offset.x) / RETURN_SPEED); // 걸리는 시간 = 거리 ÷ 속도
        launcher.style.transition = 'transform ' + seconds + 's linear';
        moveTo(0, offset.y); // 옆으로만 이동 (바닥 높이 그대로)
        restTimer = setTimeout(() => {
            launcher.classList.remove('is-walking');
            launcher.style.transition = 'transform 0.25s ease-out';
            moveTo(0, 0); // 제자리로 한 걸음
        }, seconds * 1000);
    }

    launcher.addEventListener('pointerdown', (e) => {
        if (e.button !== 0) {
            return; // 마우스 왼쪽 버튼(또는 손가락)만
        }
        stopMotion(); // 떨어지는 중·걷는 중이면 그 자리에서 잡음

        // 제자리 위치 = 지금 위치에서 떨어진 만큼 빼기
        const r = launcher.getBoundingClientRect();
        home = { left: r.left - offset.x, right: r.right - offset.x, top: r.top - offset.y, bottom: r.bottom - offset.y };

        drag = {
            id: e.pointerId,
            startX: e.clientX - offset.x, // 잡은 점과 영국이 사이 거리를 유지하려고
            startY: e.clientY - offset.y,
            downX: e.clientX,
            downY: e.clientY,
            moved: Math.hypot(offset.x, offset.y) > 2, // 제자리가 아닌 곳에서 잡았으면 누르기로 보지 않음
            captured: false,
            samples: [] // 최근 움직임 기록 (던지는 빠르기 계산용)
        };
    });

    launcher.addEventListener('pointermove', (e) => {
        if (!drag || e.pointerId !== drag.id) {
            return;
        }
        if (!drag.captured) {
            const dist = Math.hypot(e.clientX - drag.downX, e.clientY - drag.downY);
            if (!drag.moved && dist < DRAG_START) {
                return; // 아직은 살짝 흔들린 정도 → 누르기로 봄
            }
            drag.moved = true;
            drag.captured = true;
            launcher.setPointerCapture(e.pointerId); // 손이 버튼 밖으로 나가도 계속 따라오게
            launcher.classList.add('is-dragging');
        }
        moveTo(e.clientX - drag.startX, e.clientY - drag.startY);

        drag.samples.push({ x: e.clientX, y: e.clientY, t: e.timeStamp });
        if (drag.samples.length > 6) {
            drag.samples.shift(); // 최근 6개만 기억
        }
    });

    function endDrag(e) {
        if (!drag || e.pointerId !== drag.id) {
            return;
        }
        const d = drag;
        drag = null;

        if (!d.moved) {
            return; // 그냥 누르기 → 아래 click 에서 창을 엶
        }

        suppressClick = true;
        setTimeout(() => {
            suppressClick = false; // 바로 다음 클릭 한 번만 무시하고 풀기
        }, 0);

        // 놓기 직전 0.1초 동안 얼마나 빨리 움직였나 (1ms에 몇 px) → 그 빠르기로 던져짐
        // (가만히 놓았으면 0 → 그 자리에서 수직으로 떨어짐)
        const recent = d.samples.filter((s) => e.timeStamp - s.t < 100);
        let vx = 0;
        let vy = 0;
        if (recent.length >= 2) {
            const a = recent[0];
            const b = recent[recent.length - 1];
            const dt = Math.max(b.t - a.t, 1);
            vx = (b.x - a.x) / dt;
            vy = (b.y - a.y) / dt;
        }
        const speed = Math.hypot(vx, vy);
        if (speed > MAX_THROW) {
            vx = vx / speed * MAX_THROW;
            vy = vy / speed * MAX_THROW;
        }
        fall(vx, vy);
    }

    launcher.addEventListener('pointerup', endDrag);
    launcher.addEventListener('pointercancel', endDrag);

    launcher.addEventListener('click', (e) => {
        if (suppressClick) {
            e.preventDefault();
            return; // 끌거나 던진 거라 창은 열지 않음
        }
        open();
    });

    closeBtn.addEventListener('click', close);
    panel.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            close();
        }
    });

    // ===== 여기부터는 질문 주고받기 → 자는 중이면 필요 없음 (안내 글만 보임) =====
    if (sleeping) {
        return;
    }

    const sendBtn = form.querySelector('.chat-send');

    // 이 탭에서 하던 대화 불러오기
    try {
        const saved = JSON.parse(sessionStorage.getItem(STORE_KEY) || '[]');
        if (Array.isArray(saved)) {
            history = saved.filter((m) => m && (m.role === 'user' || m.role === 'model') && typeof m.text === 'string');
        }
    } catch (e) {
        history = [];
    }
    history.forEach((m) => addBubble(m.role === 'user' ? 'is-user' : 'is-bot', m.text));
    if (history.length > 0) {
        chips.hidden = true; // 이미 대화했으면 빠른 질문은 숨김
    }

    // ===== 남은 질문 수 (5번 이하일 때만) =====
    function showRemain(n) {
        if (typeof n === 'number' && n >= 0 && n <= REMAIN_SHOW) {
            remainEl.textContent = n === 0 ? '오늘 질문 횟수를 모두 쓰셨어요.' : '오늘 ' + n + '번 더 물어볼 수 있어요.';
            remainEl.hidden = false;
        } else {
            remainEl.hidden = true;
        }
    }

    // ===== 질문 보내기 =====
    async function ask(question) {
        const q = (question || '').trim();
        if (!q || busy) {
            return;
        }
        busy = true;
        sendBtn.disabled = true;
        chips.hidden = true;

        addBubble('is-user', q);
        input.value = '';
        resize();

        // "답 쓰는 중" 점 세 개 (고정된 글자라 innerHTML 써도 안전)
        const typing = addBubble('is-bot chat-typing', '');
        typing.setAttribute('aria-label', '답을 쓰는 중');
        typing.innerHTML = '<span></span><span></span><span></span>';

        try {
            const res = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: q, history: history.slice(-HISTORY_SEND) })
            });
            const data = await res.json();
            typing.remove();

            const reply = String(data.reply || '').replace(/\*\*/g, ''); // 혹시 굵은 글씨 표시(**)가 오면 지움
            if (data.ok) {
                addBubble('is-bot', reply);
                history.push({ role: 'user', text: q }, { role: 'model', text: reply });
                save();
            } else {
                addBubble('is-notice', reply); // 한도 초과·오류 안내 (대화 기록에는 안 넣음)
            }
            showRemain(data.remaining);
        } catch (e) {
            typing.remove();
            addBubble('is-notice', '연결이 원활하지 않아요. 잠시 후 다시 물어봐 주시거나 전화(' + phone + ')로 문의해 주세요.');
        } finally {
            busy = false;
            sendBtn.disabled = false;
        }
    }

    // ===== 입력 칸: 글 길이에 맞춰 높이 늘리기 (최대 약 4줄) =====
    function resize() {
        input.style.height = 'auto';
        input.style.height = Math.min(input.scrollHeight, 104) + 'px';
    }
    input.addEventListener('input', resize);

    // Enter = 보내기, Shift+Enter = 줄바꿈
    // isComposing/229: 한글 조합 중 Enter 는 무시 (안 그러면 마지막 글자가 두 번 보내짐)
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey && !e.isComposing && e.keyCode !== 229) {
            e.preventDefault();
            ask(input.value);
        }
    });

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        ask(input.value);
    });

    // 빠른 질문 버튼
    chips.addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-q]');
        if (btn) {
            ask(btn.dataset.q);
        }
    });
})();