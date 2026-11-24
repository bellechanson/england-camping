/*!
 * Lantern — Bootstrap 6 hotel & resort template (잉글랜드 캠핑장에서 쓰는 것만 남김)
 */

import { initBase, onReady } from './base.js'

/* The booking bar straddles the hero seam. Once it scrolls out of view a
   compact version docks to the top — IntersectionObserver rather than a scroll
   handler so it costs nothing while idle.
   (메인 예약 바 #bookBar 가 화면 밖으로 나가면 위에 작은 바 #bookDock 을 붙임) */
const initBookDock = () => {
    const bar = document.getElementById('bookBar')
    const dock = document.getElementById('bookDock')
    if (!bar || !dock) return

    new IntersectionObserver(
        ([entry]) => dock.classList.toggle('is-docked', !entry.isIntersecting),
        { rootMargin: '-80px 0px 0px 0px', threshold: 0 },
    ).observe(bar)

    dock.querySelector('[data-scroll-top]')?.addEventListener('click', (e) => {
        e.preventDefault()
        bar.scrollIntoView({ behavior: 'smooth', block: 'center' })
        bar.querySelector('input')?.focus({ preventScroll: true })
    })
}

onReady(() => {
    initBase()
    initBookDock()
})