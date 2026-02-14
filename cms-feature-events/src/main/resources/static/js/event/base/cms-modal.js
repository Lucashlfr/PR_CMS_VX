// ====================================================================
// cms-modal.js
// Vereinheitlichtes Verhalten für Bootstrap-Modals
// ====================================================================

function initModalBehavior() {
    document.querySelectorAll('[data-bs-toggle="modal"]').forEach(trigger => {
        trigger.addEventListener("click", () => {
            const targetModal = trigger.getAttribute("data-bs-target");
            if (!targetModal) return;

            // Verhindern, dass andere Modals automatisch geschlossen werden
            document.querySelectorAll(".modal.show").forEach(openModal => {
                if (openModal.id !== targetModal.replace("#", "")) return;
            });
        });
    });

    document.querySelectorAll(".modal").forEach(modal => {
        modal.addEventListener("hidden.bs.modal", () => {
            document.body.classList.remove("modal-open");
            document.body.style.overflow = "";
        });
    });
}
