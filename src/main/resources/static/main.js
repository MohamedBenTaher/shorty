const form = document.getElementById("shorten-form");
const urlInput = document.getElementById("url-input");
const errorEl = document.getElementById("error");
const resultDiv = document.getElementById("result");
const shortUrlInput = document.getElementById("short-url");
const copyBtn = document.getElementById("copy-btn");
const copiedMsg = document.getElementById("copied-msg");
const submitBtn = document.getElementById("submit-btn");
const buttonLabel = submitBtn.querySelector(".button-label");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const longUrl = urlInput.value.trim();

    if (!longUrl) {
        errorEl.textContent = "Add a URL first, then try again.";
        urlInput.focus();
        return;
    }

    errorEl.textContent = "";
    resultDiv.classList.add("hidden");
    copyBtn.classList.remove("copied-state");
    copiedMsg.classList.remove("show");
    submitBtn.disabled = true;
    submitBtn.classList.add("is-loading");
    buttonLabel.textContent = "Shortening...";

    try {
        const response = await fetch("/api/shorten", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ longUrl }),
        });
        const data = await response.json();

        if (!response.ok) {
            errorEl.textContent = data.error || "That link could not be shortened. Try again.";
            return;
        }

        shortUrlInput.value = data.shortUrl;
        resultDiv.classList.remove("hidden");
        resultDiv.scrollIntoView({ behavior: "smooth", block: "nearest" });
    } catch {
        errorEl.textContent = "We could not reach Shorty. Check your connection and retry.";
    } finally {
        submitBtn.disabled = false;
        submitBtn.classList.remove("is-loading");
        buttonLabel.textContent = "Shorten link";
    }
});

copyBtn.addEventListener("click", async () => {
    const url = shortUrlInput.value;
    if (!url) return;

    try {
        await navigator.clipboard.writeText(url);
        copyBtn.classList.add("copied-state");
        copiedMsg.textContent = "Copied to clipboard";
        copiedMsg.classList.add("show");
        setTimeout(() => {
            copyBtn.classList.remove("copied-state");
            copiedMsg.classList.remove("show");
        }, 1800);
    } catch {
        shortUrlInput.select();
        copiedMsg.textContent = "Select the link above to copy it";
        copiedMsg.classList.add("show");
    }
});
