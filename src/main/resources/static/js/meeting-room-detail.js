// 전역 변수는 HTML에서 주입됨 (선언하지 않음)
// meetingRoomId
// currentState

// SSE EventSource
let eventSource;

// 페이지 초기화
function initializePage() {
    console.log('🚀 Initializing page with meetingRoomId:', meetingRoomId);
    console.log('📊 Initial state:', currentState);

    // SSE 연결
    eventSource = new EventSource(`/api/meeting-rooms/${meetingRoomId}/subscribe`);

    eventSource.addEventListener('connect', (event) => {
        console.log('✅ SSE Connected:', event.data);
    });

    eventSource.addEventListener('update', (event) => {
        const data = JSON.parse(event.data);
        console.log('🔄 SSE Update received:', data);
        updateUI(data);
    });

    eventSource.onerror = (error) => {
        console.error('❌ SSE Error:', error);
    };

    // 초기 진행률 설정
    updateProgress();
}

// UI 업데이트 함수
function updateUI(data) {
    currentState = {
        airConditionerOff: data.airConditionerOff,
        tvOff: data.tvOff,
        lightOff: data.lightOff,
        trashCleaned: data.trashCleaned
    };

    // 각 체크박스 및 상태 업데이트
    updateCheckboxUI('airConditionerOff', data.airConditionerOff);
    updateCheckboxUI('tvOff', data.tvOff);
    updateCheckboxUI('lightOff', data.lightOff);
    updateCheckboxUI('trashCleaned', data.trashCleaned);

    // 진행률 업데이트
    updateProgress();
}

// 체크박스 UI 업데이트
function updateCheckboxUI(field, checked) {
    const checkboxElement = document.getElementById('checkbox-' + field);
    const statusElement = document.getElementById('status-' + field);
    
    if (checked) {
        checkboxElement.classList.add('checked');
        statusElement.classList.remove('pending');
        statusElement.classList.add('completed');
        statusElement.textContent = '완료';
    } else {
        checkboxElement.classList.remove('checked');
        statusElement.classList.remove('completed');
        statusElement.classList.add('pending');
        statusElement.textContent = '대기';
    }
}

// 진행률 업데이트
function updateProgress() {
    const total = 4;
    const completed = Object.values(currentState).filter(v => v === true).length;
    const percentage = (completed / total) * 100;

    const progressBar = document.getElementById('progressBar');
    progressBar.style.width = percentage + '%';
    progressBar.textContent = completed + ' / ' + total;
}

// 개별 체크박스 토글
async function toggleCheckbox(field) {
    const newValue = !currentState[field];

    console.log(`🔄 Toggling ${field}: ${currentState[field]} → ${newValue}`);

    try {
        const response = await fetch(`/api/meeting-rooms/${meetingRoomId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                [field]: newValue
            })
        });

        if (!response.ok) {
            throw new Error('업데이트 실패: ' + response.status);
        }

        const updatedData = await response.json();
        console.log('✅ Update successful:', updatedData);

        // SSE를 통해 자동으로 업데이트되지만, 즉각적인 피드백을 위해 로컬 업데이트
        currentState[field] = newValue;
        updateCheckboxUI(field, newValue);
        updateProgress();

    } catch (error) {
        console.error('❌ Error updating:', error);
        alert('업데이트 중 오류가 발생했습니다: ' + error.message);
    }
}

// 전체 토글
async function toggleAll() {
    // 현재 모든 항목이 체크되어 있는지 확인
    const allChecked = Object.values(currentState).every(v => v === true);
    const newValue = !allChecked;

    console.log(`🔄 Toggle All: ${allChecked} → ${newValue}`);

    try {
        const response = await fetch(`/api/meeting-rooms/${meetingRoomId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                airConditionerOff: newValue,
                tvOff: newValue,
                lightOff: newValue,
                trashCleaned: newValue
            })
        });

        if (!response.ok) {
            throw new Error('업데이트 실패: ' + response.status);
        }

        const updatedData = await response.json();
        console.log('✅ Toggle All successful:', updatedData);

        // SSE를 통해 업데이트될 예정이지만 즉각 반영
        updateUI(updatedData);

    } catch (error) {
        console.error('❌ Error toggling all:', error);
        alert('전체 업데이트 중 오류가 발생했습니다: ' + error.message);
    }
}

// 페이지 로드 시 초기화
window.addEventListener('DOMContentLoaded', () => {
    initializePage();
});

// 페이지 언로드 시 SSE 연결 종료
window.addEventListener('beforeunload', () => {
    if (eventSource) {
        eventSource.close();
    }
});
