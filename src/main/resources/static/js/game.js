const autoAdvanceElement =
    document.getElementById("auto-advance");

if (autoAdvanceElement) {
    const nextUrl = autoAdvanceElement.dataset.nextUrl;

    window.setTimeout(() => {
        window.location.assign(nextUrl);
    }, 2500);
}