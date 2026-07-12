const form = document.getElementById("shorten-form");
const urlInput = document.getElementById("url-input");
const errorEl = document.getElementById("error");
const resultDiv = document.getElementById("result");
const shortUrlInput = document.getElementById("short-url");
const copyBtn = document.getElementById("copy-btn");
const copiedMsg = document.getElementById("copied-msg");

form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const longUrl = urlInput.value.trim();
    if (!longUrl) {
        errorEl.textContent = "Please enter a URL.";
        return;
    }

    errorEl.textContent = "";
    resultDiv.classList.add("hidden");
    copyBtn.classList.remove("copied-state");
    copiedMsg.classList.remove("show");

    try {
        const res = await fetch("/api/urls", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ longUrl }),
        });

        const data = await res.json();

        if (!res.ok) {
            errorEl.textContent = data.error || "Something went wrong.";
            return;
        }

        shortUrlInput.value = data.shortUrl;
        resultDiv.classList.remove("hidden");
    } catch {
        errorEl.textContent = "Network error. Please try again.";
    }
});

copyBtn.addEventListener("click", () => {
    const url = shortUrlInput.value;
    if (!url) return;

    navigator.clipboard.writeText(url).then(() => {
        copyBtn.classList.add("copied-state");
        copiedMsg.classList.add("show");
        setTimeout(() => {
            copyBtn.classList.remove("copied-state");
            copiedMsg.classList.remove("show");
        }, 1800);
    });
});
