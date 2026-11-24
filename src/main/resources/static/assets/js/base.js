/*!
 * Bootstrap 6 template kit — shared base JS (잉글랜드 캠핑장에서 쓰는 것만 남김)
 * Bootstrap 6.0.0-alpha1 (pinned 59cee6c)
 *
 * Bootstrap 6 is ESM-only: importing the bundle for its side effects is what
 * registers the data-attribute API (data-bs-toggle="dialog" | "drawer" …).
 * → 아래 import 가 있어야 팝업(dialog)·폰 메뉴 서랍(drawer)이 동작함. 지우면 안 됨
 */

import 'bootstrap'

/* Sticky header — CSS position:sticky can't report when it stuck, so watch a
   zero-height sentinel at the top of the document.
   (스크롤하면 머리글에 is-stuck → 크림색 바탕) */
export const initStickyHeader = () => {
    const header = document.querySelector('.site-header')
    if (!header) return

    const sentinel = document.createElement('div')
    sentinel.setAttribute('aria-hidden', 'true')
    document.body.prepend(sentinel)

    new IntersectionObserver(
        ([entry]) => header.classList.toggle('is-stuck', !entry.isIntersecting),
        { threshold: 0 }
    ).observe(sentinel)
}

/* Close the mobile drawer after tapping an in-page link (#로 시작하는 링크) */
export const initDrawerAutoClose = () => {
    document.querySelectorAll('dialog.drawer').forEach((drawer) => {
        drawer.addEventListener('click', (e) => {
            if (e.target.closest('a[href^="#"]')) drawer.close()
        })
    })
}

/* 꼬리말 © 연도 (data-year) */
export const initYear = () => {
    document.querySelectorAll('[data-year]')
        .forEach((el) => { el.textContent = String(new Date().getFullYear()) })
}

/* 모든 화면 공통 */
export const initBase = () => {
    initStickyHeader()
    initDrawerAutoClose()
    initYear()
}

export const onReady = (fn) => {
    document.readyState === 'loading'
        ? document.addEventListener('DOMContentLoaded', fn)
        : fn()
}