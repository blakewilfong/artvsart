const { test } = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/game.js'), 'utf8');

function setup({ advance = true, event = true } = {}) {
    const timers = new Map();
    const navigations = [];
    let nextId = 0;
    function element() {
        return { hidden: true, textContent: '', attributes: {}, listeners: {},
            setAttribute(key, value) { this.attributes[key] = value; },
            addEventListener(type, handler) { this.listeners[type] = handler; } };
    }
    const control = element(), link = element(), status = element();
    const window = {
        setTimeout(handler, delay) { assert.equal(delay, 2500); timers.set(++nextId, handler); return nextId; },
        clearTimeout(id) { timers.delete(id); },
        location: { assign(url) { navigations.push(url); } }
    };
    const document = {
        getElementById() { return advance ? { dataset: { nextUrl: '/wager/continue' } } : null; },
        querySelectorAll(selector) {
            if (!event) return [];
            return selector.includes('control') ? [control] : selector.includes('status') ? [status] : [link];
        }
    };
    vm.runInNewContext(source, { window, document });
    return { timers, navigations, control, link, status };
}

test('unanswered, completed, and manual-continue screens do not schedule navigation', () => {
    const state = setup({ advance: false });
    assert.equal(state.timers.size, 0);
    assert.equal(state.control.hidden, true);
});

test('ordinary rounds still automatically advance', () => {
    const state = setup({ event: false });
    assert.equal(state.timers.size, 1);
    [...state.timers.values()][0]();
    assert.deepEqual(state.navigations, ['/wager/continue']);
});

test('pause cancels navigation and resume schedules just one fresh timer', () => {
    const state = setup();
    assert.equal(state.control.hidden, false);
    state.control.listeners.click();
    assert.equal(state.timers.size, 0);
    assert.equal(state.control.attributes['aria-pressed'], 'true');
    assert.equal(state.status.textContent, 'Paused for reading');
    state.control.listeners.click();
    assert.equal(state.timers.size, 1);
    assert.equal(state.control.attributes['aria-pressed'], 'false');
});

for (const action of ['focus', 'click', 'auxclick']) {
    test(`Wikipedia ${action} pauses without suppressing the link`, () => {
        const state = setup();
        state.link.listeners[action]();
        state.link.listeners[action]();
        assert.equal(state.timers.size, 0);
        assert.equal(state.control.textContent, 'Resume auto-advance');
        assert.deepEqual(state.navigations, []);
    });
}
