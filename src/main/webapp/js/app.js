// QueueSense Frontend Javascript Suite

document.addEventListener('DOMContentLoaded', function () {
    // Session Timer Logic
    const timerElem = document.getElementById('serviceTimer');
    if (timerElem) {
        let elapsedSeconds = parseInt(timerElem.getAttribute('data-initial-seconds') || '0', 10);
        const isPaused = timerElem.getAttribute('data-is-paused') === 'true';

        function formatTime(secs) {
            const m = Math.floor(secs / 60);
            const s = secs % 60;
            return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
        }

        timerElem.textContent = formatTime(elapsedSeconds);

        if (!isPaused) {
            setInterval(function () {
                elapsedSeconds++;
                timerElem.textContent = formatTime(elapsedSeconds);
            }, 1000);
        }
    }
});

// Dynamic Chart.js Analytics Loader
function initDashboardCharts(filter, startDate, endDate) {
    const url = new URL(window.location.origin + window.location.pathname.replace('/dashboard', '') + '/api/analytics/charts');
    if (filter) url.searchParams.append('filter', filter);
    if (startDate) url.searchParams.append('startDate', startDate);
    if (endDate) url.searchParams.append('endDate', endDate);

    fetch(url)
        .then(response => response.json())
        .then(data => {
            renderChart('servedByHourChart', 'bar', data.servedByHour, '#3b82f6');
            renderChart('waitTimeByHourChart', 'line', data.waitTimeByHour, '#f59e0b');
            renderChart('serviceDemandChart', 'doughnut', data.serviceDemand, ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#64748b']);
            renderChart('tokensByPriorityChart', 'pie', data.tokensByPriority, ['#0284c7', '#f59e0b', '#ea580c', '#dc2626']);
            renderChart('counterUtilizationChart', 'bar', data.counterUtilization, '#10b981');
            renderChart('outcomeChart', 'doughnut', data.outcomeDistribution, ['#10b981', '#ef4444', '#a855f7']);
            renderChart('avgServiceTimeChart', 'bar', data.avgServiceTime, '#8b5cf6');
            renderChart('dailyVolumeChart', 'line', data.dailyVolume, '#2563eb');
            renderChart('peakHoursChart', 'bar', data.peakHours, '#6366f1');
            renderChart('satisfactionChart', 'bar', data.satisfaction, '#facc15');
        })
        .catch(err => console.error('Error loading analytics charts:', err));
}

function renderChart(canvasId, type, chartDto, bgColors) {
    const ctx = document.getElementById(canvasId);
    if (!ctx || !chartDto) return;

    if (ctx.chartInstance) {
        ctx.chartInstance.destroy();
    }

    const isArrayColor = Array.isArray(bgColors);

    ctx.chartInstance = new Chart(ctx, {
        type: type,
        data: {
            labels: chartDto.labels,
            datasets: [{
                label: chartDto.datasetLabel,
                data: chartDto.data,
                backgroundColor: isArrayColor ? bgColors : bgColors,
                borderColor: type === 'line' ? bgColors : '#ffffff',
                borderWidth: type === 'line' ? 2 : 1,
                fill: type === 'line' ? false : undefined,
                tension: 0.2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: type === 'pie' || type === 'doughnut'
                }
            },
            scales: (type === 'pie' || type === 'doughnut') ? {} : {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 }
                }
            }
        }
    });
}
