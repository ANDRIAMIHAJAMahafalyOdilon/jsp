<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="topbarSearch">
    <div class="live-search-wrap">
        <input id="reservationsLiveSearch"
               class="form-control form-control-sm pe-5"
               placeholder="Rechercher reservation..."
               autocomplete="off" />
        <button id="reservationsLiveSearchClear" class="live-search-clear" type="button" aria-label="Effacer recherche">&times;</button>
    </div>
</c:set>
<c:set var="topbarActions">
    <a class="btn btn-outline-dark btn-sm d-flex align-items-center gap-1"
       target="_blank"
       href="${pageContext.request.contextPath}/travellers-report">
        <i class="bi bi-filetype-pdf text-danger"></i>
        <span>PDF voyageurs</span>
    </a>
</c:set>
<%@ include file="../common/header.jspf" %>

<style>
    #reservationDrawer form {
        height: calc(100vh - 72px);
    }
    #reservationDrawer .offcanvas-body {
        flex: 1 1 auto;
        overflow-y: auto;
        padding-bottom: 5.5rem;
    }
    #reservationDrawer .offcanvas-footer {
        position: sticky;
        bottom: 0;
        background: #fff;
        border-top: 1px solid #e5e7eb;
        z-index: 2;
        flex-shrink: 0;
    }

    /* Same style as dashboard "Dernieres reservations" */
    .status-badge { font-size: .78rem; padding: 5px 12px; border-radius: 20px; font-weight: 600; }

    .live-search-wrap { position: relative; min-width: 260px; }
    .live-search-clear {
        position: absolute;
        right: 8px;
        top: 50%;
        transform: translateY(-50%);
        border: none;
        background: transparent;
        color: #6b7280;
        font-size: 1rem;
        width: 26px;
        height: 26px;
        border-radius: 50%;
        display: none;
    }
    .live-search-clear:hover { background: #f1f5f9; }

    html, body { overflow-x: hidden !important; }
    .topbar { overflow-x: hidden !important; }
    .app-shell { overflow-x: hidden !important; }

    .places-grid-res {
        display: grid;
        grid-template-columns: repeat(4, minmax(64px, 1fr));
        gap: 8px;
        max-width: 360px;
    }
    .places-grid-res .place-btn.driver-seat {
        grid-column: span 2;
        font-weight: 700;
    }
    .places-grid-res .place-btn.selected {
        background-color: #dc3545 !important;
        border-color: #dc3545 !important;
        color: #fff !important;
    }
    .places-grid-res .place-btn.occupied {
        opacity: 0.45;
        pointer-events: none;
    }

    #resMontantAvance::-webkit-outer-spin-button,
    #resMontantAvance::-webkit-inner-spin-button {
        -webkit-appearance: none;
        margin: 0;
    }
    #resMontantAvance {
        -moz-appearance: textfield;
        appearance: textfield;
    }
    #resMontantAvance.montant-avance-frozen {
        background-color: #e9ecef;
        color: #495057;
        cursor: not-allowed;
        pointer-events: none;
    }
</style>

<c:set var="isUpdateForm" value="${formAction == 'update' || (empty formAction and not empty reservationEdit and empty reservationForm)}" />
<c:set var="rf" value="${not empty reservationEdit ? reservationEdit : reservationForm}" />
<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">${errorMessage}</div>
</c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <button class="btn btn-primary rounded-3 d-flex align-items-center gap-2"
            data-bs-toggle="offcanvas" data-bs-target="#reservationDrawer" aria-controls="reservationDrawer">
        <i class="bi bi-plus-lg"></i>
        <span>Ajouter</span>
    </button>
</div>

<div class="card border-0 shadow-sm rounded-4 page-block">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="card-title mb-0">Reservations</h5>
            <span class="badge text-bg-light">${fn:length(reservations)} enregistrements</span>
        </div>
        <div class="table-responsive" style="min-height: 62vh;">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                <tr>
                    <th>ID</th>
                    <th>Client</th>
                    <th>Voiture</th>
                    <th>Place</th>
                    <th>Date Reservation</th>
                    <th>Date Voyage</th>
                    <th>Paiement</th>
                    <th>Avance</th>
                    <th>Reste</th>
                    <th class="text-center">Action</th>
                </tr>
                </thead>
                <tbody id="reservationsTableBody">
                <c:forEach var="r" items="${reservations}">
                    <tr>
                        <td class="fw-semibold">${r.idReserv}</td>
                        <td>
                            <div class="fw-bold">${r.nomClient}</div>
                            <small class="text-muted">${r.numTel}</small>
                        </td>
                        <td>
                            <span class="badge bg-secondary text-uppercase">${r.typeVoiture}</span>
                            (${r.idVoit})
                        </td>
                        <td><span class="badge text-bg-dark">Place ${r.place}</span></td>
                        <td>${r.dateReserv}</td>
                        <td>${r.dateVoyage}</td>
                        <td>
                            <c:choose>
                                <c:when test="${r.paiement == 'Tout payé'}">
                                    <span class="status-badge bg-success-subtle text-success">Tout payé</span>
                                </c:when>
                                <c:when test="${r.paiement == 'Avec avance'}">
                                    <span class="status-badge bg-warning-subtle text-warning">Avec avance</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge bg-danger-subtle text-danger">Sans avance</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td><fmt:formatNumber value="${r.montantAvance}" type="number" groupingUsed="true" /> Ar</td>
                        <td><fmt:formatNumber value="${r.reste}" type="number" groupingUsed="true" /> Ar</td>
                        <td class="text-center">
                            <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/reservations?action=edit&id=${r.idReserv}" title="Modifier"
                               data-confirm="Modifier cette reservation ?">
                                <i class="bi bi-pencil text-primary"></i>
                            </a>
                            <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/receipt?id=${r.idReserv}" target="_blank" title="PDF">
                                <i class="bi bi-filetype-pdf text-danger"></i>
                            </a>
                            <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/reservations?action=delete&id=${r.idReserv}" title="Supprimer"
                               data-confirm="Supprimer cette reservation ?">
                                <i class="bi bi-trash text-danger"></i>
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty reservations}">
                    <tr><td colspan="10" class="text-center text-muted py-4">Aucune reservation disponible.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
        <nav class="mt-3">
            <ul class="pagination pagination-sm justify-content-end mb-0">
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/reservations?page=${p}">${p}</a>
                    </li>
                </c:forEach>
            </ul>
        </nav>
    </div>
</div>

<div class="offcanvas offcanvas-end" tabindex="-1" id="reservationDrawer" aria-labelledby="reservationDrawerLabel">
    <div class="offcanvas-header border-bottom">
        <h5 class="offcanvas-title" id="reservationDrawerLabel">${isUpdateForm ? 'Modifier reservation' : 'Nouvelle reservation'}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/reservations" class="d-flex flex-column h-100">
        <div class="offcanvas-body">
            <input type="hidden" name="action" value="${isUpdateForm ? 'update' : 'create'}" />
            <input type="hidden" name="old_idvoit" value="${rf.idVoit}" />
            <input type="hidden" name="old_place" value="${rf.place}" />
            <c:if test="${isUpdateForm}">
                <div class="mb-2">
                    <label class="form-label">ID reservation</label>
                    <input type="hidden" name="idreserv" value="${rf.idReserv}" />
                    <input class="form-control" value="${rf.idReserv}" readonly />
                </div>
            </c:if>
            <c:if test="${!isUpdateForm}">
                <div class="mb-2">
                    <label class="form-label">ID reservation</label>
                    <input class="form-control ${not empty idReservError ? 'is-invalid' : ''}"
                           id="reservationIdInput"
                           name="idreserv"
                           value="${rf.idReserv}"
                           placeholder="Ex: RES-001"
                           required
                           autocomplete="off"
                           data-manual-invalid="true" />
                    <div id="reservationIdFeedback" class="invalid-feedback d-block" style="${empty idReservError ? 'display:none' : 'display:block'}">${idReservError}</div>
                </div>
            </c:if>
            <div class="mb-2">
                <label class="form-label">Voiture</label>
                <select class="form-select" id="resVoitureSelect" name="idvoit" required>
                    <c:forEach var="v" items="${voitures}">
                        <option value="${v.idVoit}"
                                data-nbrplace="${v.nbrPlace}"
                                data-frais="${v.frais}"
                                ${(rf.idVoit == v.idVoit || (empty rf.idVoit and selectedVoitureFilter == v.idVoit)) ? 'selected' : ''}>${v.idVoit} - ${v.design} (${v.type})</option>
                    </c:forEach>
                </select>
            </div>
            <div class="mb-2">
                <label class="form-label">Client</label>
                <select class="form-select" name="idcli" required>
                    <c:forEach var="c" items="${clients}">
                        <option value="${c.idCli}" ${rf.idCli == c.idCli ? 'selected' : ''}>${c.nom} - ${c.numTel}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="mb-2" id="placePickerWrap">
                <label class="form-label">Place</label>
                <input type="hidden" name="places" id="resPlacesHidden" value="${selectedPlacesCsv}" />
                <input type="hidden" name="place" id="resPlaceSingle" value="${isUpdateForm ? rf.place : ''}" />
                <button type="button" class="btn btn-outline-primary w-100" id="resPlaceOpenBtn">
                    <i class="bi bi-grid-3x3-gap me-1"></i> Choisir les places
                </button>
                <input class="form-control mt-2" type="text" id="resPlaceDisplay" readonly placeholder="Aucune place selectionnee" />
                <div class="invalid-feedback" id="resPlaceFeedback" style="display:none;">Veuillez selectionner au moins une place.</div>
            </div>
            <div class="mb-2">
                <label class="form-label">Date reservation</label>
                <input class="form-control"
                       type="datetime-local"
                       step="1"
                       name="date_reserv"
                       value="${fn:replace(rf.dateReserv, ' ', 'T')}"
                       required />
                <c:if test="${not empty dateReservError}">
                    <div class="text-danger small mt-1">${dateReservError}</div>
                </c:if>
            </div>
            <div class="mb-2">
                <label class="form-label">Date de voyage</label>
                <input class="form-control" type="date" name="date_voyage" value="${rf.dateVoyage}" required />
                <c:if test="${not empty dateVoyageError}">
                    <div class="text-danger small mt-1">${dateVoyageError}</div>
                </c:if>
            </div>
            <div class="mb-2">
                <label class="form-label">Paiement</label>
                <select class="form-select" id="resPaiementSelect" name="paiement" required>
                    <option value="Sans avance" ${rf.paiement == 'Sans avance' ? 'selected' : ''}>Sans avance</option>
                    <option value="Avec avance" ${rf.paiement == 'Avec avance' ? 'selected' : ''}>Avec avance</option>
                    <option value="Tout payé" ${rf.paiement == 'Tout payé' ? 'selected' : ''}>Tout payé</option>
                </select>
            </div>
            <div class="mb-2">
                <label class="form-label">Montant avance</label>
                <input type="hidden" name="montant_avance" id="resMontantAvanceHidden" value="${rf.montantAvance}" />
                <input class="form-control"
                       type="number"
                       id="resMontantAvance"
                       min="0"
                       value="${rf.montantAvance}"
                       autocomplete="off" />
                <div class="text-danger small mt-1 d-none" id="resMontantAvanceFeedback">Montant avance invalide pour ce mode de paiement.</div>
                <c:if test="${not empty montantAvanceError}">
                    <div class="text-danger small mt-1">${montantAvanceError}</div>
                </c:if>
            </div>
        </div>
        <div class="offcanvas-footer p-3 d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-light" data-bs-dismiss="offcanvas">Annuler</button>
            <button class="btn btn-primary" type="submit">Enregistrer</button>
        </div>
    </form>
</div>

<div class="modal fade" id="placePickerModal" tabindex="-1" aria-labelledby="placePickerModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header">
                <h5 class="modal-title" id="placePickerModalLabel">Selection des places</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p class="text-muted small mb-3">Cliquez pour selectionner. Double-clic pour deselectionner. Les places choisies sont en rouge.</p>
                <div class="places-grid-res mx-auto" id="resPlaceGrid"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-light" id="resPlaceCancel" data-bs-dismiss="modal">Annuler</button>
                <button type="button" class="btn btn-primary" id="resPlaceValidate">Valider</button>
            </div>
        </div>
    </div>
</div>

<c:if test="${not empty reservationEdit or not empty formAction or openReservationDrawer}">
    <script>
        window.addEventListener("DOMContentLoaded", function () {
            var drawerElement = document.getElementById("reservationDrawer");
            if (drawerElement) {
                new bootstrap.Offcanvas(drawerElement).show();
            }
        });
    </script>
</c:if>

<script>
    window.addEventListener("DOMContentLoaded", function () {
        if (typeof bootstrap === "undefined") return;

        var ctx = "${pageContext.request.contextPath}";
        var isUpdate = ${isUpdateForm ? 'true' : 'false'};
        var wrap = document.getElementById("placePickerWrap");
        if (!wrap) return;

        var voitureSelect = document.getElementById("resVoitureSelect");
        var openBtn = document.getElementById("resPlaceOpenBtn");
        var display = document.getElementById("resPlaceDisplay");
        var modalEl = document.getElementById("placePickerModal");
        var grid = document.getElementById("resPlaceGrid");
        var hiddenPlaces = document.getElementById("resPlacesHidden");
        var hiddenSingle = document.getElementById("resPlaceSingle");
        var btnValidate = document.getElementById("resPlaceValidate");
        var feedback = document.getElementById("resPlaceFeedback");
        var form = wrap.closest("form");
        var placeModal = null;

        function getPlaceModal() {
            if (!modalEl) return null;
            if (!placeModal) {
                placeModal = bootstrap.Modal.getOrCreateInstance(modalEl);
            }
            return placeModal;
        }

        var confirmed = [];
        var draft = [];
        var lastFreePlaces = [];

        function parseList(raw) {
            if (!raw) return [];
            return raw.split(",").map(function (x) { return Number(x.trim()); }).filter(function (n) { return Number.isFinite(n) && n > 0; });
        }

        function syncHidden() {
            if (isUpdate) {
                hiddenSingle.value = confirmed.length ? String(confirmed[0]) : "";
                hiddenPlaces.value = "";
            } else {
                hiddenPlaces.value = confirmed.join(",");
                hiddenSingle.value = confirmed.length === 1 ? String(confirmed[0]) : "";
            }
        }

        function updateDisplay() {
            if (!confirmed.length) {
                display.value = "";
                display.placeholder = "Aucune place selectionnee";
                return;
            }
            display.value = confirmed.length === 1
                ? ("Place " + confirmed[0])
                : ("Places " + confirmed.join(", "));
        }

        function renderGrid(freePlaces) {
            lastFreePlaces = freePlaces || [];
            var opt = voitureSelect.options[voitureSelect.selectedIndex];
            var total = Number(opt ? opt.getAttribute("data-nbrplace") : 0) || 0;
            var freeSet = new Set(lastFreePlaces.map(Number));
            if (isUpdate && hiddenSingle.value) {
                var current = Number(hiddenSingle.value);
                if (Number.isFinite(current) && current > 0) {
                    freeSet.add(current);
                }
            }
            grid.innerHTML = "";
            if (total <= 0) {
                grid.innerHTML = "<div class=\"text-muted small\">Aucune place pour cette voiture.</div>";
                return;
            }
            for (var p = 1; p <= total; p++) {
                var btn = document.createElement("button");
                btn.type = "button";
                btn.className = "btn btn-sm rounded-3 place-btn";
                btn.dataset.place = String(p);
                if (p === 1) {
                    btn.classList.add("driver-seat");
                    btn.textContent = "Chauffeur";
                } else {
                    btn.textContent = String(p).padStart(2, "0");
                }
                var occupied = !freeSet.has(p);
                if (occupied) {
                    btn.classList.add("occupied", "btn-secondary");
                } else {
                    btn.classList.add("btn-outline-success");
                }
                if (draft.indexOf(p) !== -1) {
                    btn.classList.add("selected");
                }
                if (!occupied) {
                    (function (placeNum, button) {
                        button.addEventListener("click", function () {
                            var idx = draft.indexOf(placeNum);
                            if (idx === -1) {
                                if (isUpdate) {
                                    draft = [placeNum];
                                } else {
                                    draft.push(placeNum);
                                }
                                draft.sort(function (a, b) { return a - b; });
                            } else {
                                draft.splice(idx, 1);
                            }
                            renderGrid(lastFreePlaces);
                        });
                        button.addEventListener("dblclick", function (e) {
                            e.preventDefault();
                            var i = draft.indexOf(placeNum);
                            if (i !== -1) {
                                draft.splice(i, 1);
                                renderGrid(lastFreePlaces);
                            }
                        });
                    })(p, btn);
                }
                grid.appendChild(btn);
            }
        }

        function loadPlaces(openModal) {
            var idVoit = voitureSelect.value;
            if (!idVoit) return;
            fetch(ctx + "/places?action=list&idvoit=" + encodeURIComponent(idVoit), { headers: { "Accept": "application/json" } })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    var free = (data && data.ok && Array.isArray(data.freePlaces)) ? data.freePlaces : [];
                    renderGrid(free);
                    if (openModal) {
                        var m = getPlaceModal();
                        if (m) m.show();
                    }
                })
                .catch(function () {
                    renderGrid([]);
                    if (openModal) {
                        var m = getPlaceModal();
                        if (m) m.show();
                    }
                });
        }

        if (openBtn) {
            openBtn.addEventListener("click", function () {
                if (!voitureSelect.value) return;
                draft = confirmed.slice();
                loadPlaces(true);
            });
        }

        if (modalEl) {
            modalEl.addEventListener("hidden.bs.modal", function () {
                draft = confirmed.slice();
            });
        }

        if (btnValidate) {
            btnValidate.addEventListener("click", function () {
                if (!draft.length) {
                    feedback.classList.remove("d-none");
                    return;
                }
                feedback.classList.add("d-none");
                confirmed = draft.slice();
                syncHidden();
                updateDisplay();
                display.classList.remove("is-invalid");
                var m = getPlaceModal();
                if (m) m.hide();
            });
        }

        voitureSelect.addEventListener("change", function () {
            confirmed = [];
            draft = [];
            syncHidden();
            updateDisplay();
        });

        if (form) {
            form.addEventListener("submit", function (e) {
                if (!confirmed.length) {
                    feedback.classList.remove("d-none");
                    display.classList.add("is-invalid");
                    e.preventDefault();
                    e.stopPropagation();
                }
            }, true);
        }

        if (isUpdate && hiddenSingle.value) {
            confirmed = parseList(hiddenSingle.value);
        } else if (hiddenPlaces.value) {
            confirmed = parseList(hiddenPlaces.value);
        }
        draft = confirmed.slice();
        syncHidden();
        updateDisplay();
    });
</script>

<script>
    (function () {
        var idInput = document.getElementById("reservationIdInput");
        var feedback = document.getElementById("reservationIdFeedback");
        if (!idInput || !feedback) return;
        var ctx = "${pageContext.request.contextPath}";
        var timer = null;

        function setFeedback(msg, invalid) {
            if (!msg) {
                feedback.style.display = "none";
                feedback.textContent = "";
                idInput.classList.remove("is-invalid");
                idInput.removeAttribute("data-id-invalid");
                return;
            }
            feedback.textContent = msg;
            feedback.style.display = "block";
            if (invalid) {
                idInput.classList.add("is-invalid");
                idInput.setAttribute("data-id-invalid", "true");
            } else {
                idInput.classList.remove("is-invalid");
                idInput.removeAttribute("data-id-invalid");
            }
        }

        function checkId() {
            var id = (idInput.value || "").trim();
            if (!id) {
                setFeedback(null, false);
                return;
            }
            fetch(ctx + "/reservations?action=checkId&id=" + encodeURIComponent(id), { headers: { "Accept": "application/json" } })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    if (data && data.exists) {
                        setFeedback("Cet ID reservation existe deja.", true);
                    } else {
                        setFeedback(null, false);
                    }
                })
                .catch(function () {});
        }

        idInput.addEventListener("input", function () {
            clearTimeout(timer);
            timer = setTimeout(checkId, 350);
        });
        idInput.addEventListener("blur", checkId);

        var form = idInput.closest("form");
        if (form) {
            form.addEventListener("submit", function (e) {
                if (idInput.getAttribute("data-id-invalid") === "true") {
                    e.preventDefault();
                    e.stopPropagation();
                    feedback.style.display = "block";
                }
            }, true);
        }

        if (feedback.textContent && feedback.textContent.trim()) {
            idInput.setAttribute("data-id-invalid", "true");
        }
    })();
</script>

<script>
    (function () {
        var input = document.getElementById("reservationsLiveSearch");
        var clearBtn = document.getElementById("reservationsLiveSearchClear");
        var tbody = document.getElementById("reservationsTableBody");
        if (!input || !clearBtn || !tbody) return;

        function filterRows() {
            var term = (input.value || "").toLowerCase().trim();
            var rows = tbody.querySelectorAll("tr");
            rows.forEach(function (row) {
                var txt = (row.textContent || "").toLowerCase();
                row.style.display = term === "" || txt.indexOf(term) !== -1 ? "" : "none";
            });
            clearBtn.style.display = term === "" ? "none" : "inline-flex";
        }

        function resetFilter() {
            if (!input.value) return;
            input.value = "";
            filterRows();
        }

        input.addEventListener("input", filterRows);
        clearBtn.addEventListener("click", function () {
            resetFilter();
            input.focus();
        });
        document.addEventListener("click", function (e) {
            if (input.contains(e.target) || clearBtn.contains(e.target)) return;
            resetFilter();
        });

        filterRows();
    })();
</script>

<script>
    window.addEventListener("DOMContentLoaded", function () {
        var paiementSelect = document.getElementById("resPaiementSelect");
        var montantInput = document.getElementById("resMontantAvance");
        var montantHidden = document.getElementById("resMontantAvanceHidden");
        var montantFeedback = document.getElementById("resMontantAvanceFeedback");
        var voitureSelect = document.getElementById("resVoitureSelect");
        var form = montantInput ? montantInput.closest("form") : null;
        if (!paiementSelect || !montantInput || !montantHidden || !voitureSelect) return;

        var lastEditableValue = montantInput.value || "";

        function normalizeMode(raw) {
            var s = (raw || "").toLowerCase().replace(/é|è|ê/g, "e").trim();
            if (s.indexOf("sans") !== -1) return "sans";
            if (s.indexOf("tout") !== -1) return "tout";
            return "avec";
        }

        function getFrais() {
            var opt = voitureSelect.options[voitureSelect.selectedIndex];
            return Number(opt ? opt.getAttribute("data-frais") : 0) || 0;
        }

        function syncHidden() {
            montantHidden.value = montantInput.value === "" ? "0" : String(montantInput.value);
        }

        function setFrozen(frozen) {
            montantInput.disabled = frozen;
            montantInput.classList.toggle("montant-avance-frozen", frozen);
            montantInput.tabIndex = frozen ? -1 : 0;
        }

        function applyPaiementMode(preserveAvance) {
            var frais = getFrais();
            var mode = normalizeMode(paiementSelect.value);
            montantInput.classList.remove("is-invalid");
            if (montantFeedback) montantFeedback.classList.add("d-none");

            if (mode === "tout") {
                montantInput.value = String(frais);
                setFrozen(true);
                montantInput.placeholder = "";
                montantInput.removeAttribute("min");
                montantInput.removeAttribute("max");
            } else if (mode === "sans") {
                montantInput.value = "0";
                setFrozen(true);
                montantInput.placeholder = "";
                montantInput.removeAttribute("min");
                montantInput.removeAttribute("max");
            } else {
                setFrozen(false);
                montantInput.placeholder = frais > 0
                    ? ("Montant total : " + frais.toLocaleString("fr-FR") + " Ar")
                    : "Montant total : —";
                montantInput.setAttribute("min", "1");
                montantInput.removeAttribute("max");
                if (!preserveAvance) {
                    montantInput.value = lastEditableValue && lastEditableValue !== "0"
                        ? lastEditableValue
                        : "";
                }
            }
            syncHidden();
        }

        function validateMontantForSubmit() {
            var frais = getFrais();
            var mode = normalizeMode(paiementSelect.value);
            applyPaiementMode(mode === "avec");
            if (mode === "sans" || mode === "tout") {
                return true;
            }
            /* "Avec avance" : avance doit être > 0 */
            var avance = Number(montantInput.value);
            if (!Number.isFinite(avance) || avance <= 0) {
                montantInput.classList.add("is-invalid");
                if (montantFeedback) montantFeedback.classList.remove("d-none");
                return false;
            }
            return true;
        }

        paiementSelect.addEventListener("change", function () {
            if (!montantInput.disabled && montantInput.value) {
                lastEditableValue = montantInput.value;
            }
            applyPaiementMode(false);
        });

        voitureSelect.addEventListener("change", function () {
            applyPaiementMode(normalizeMode(paiementSelect.value) === "avec");
        });

        montantInput.addEventListener("input", function () {
            if (!montantInput.disabled) {
                lastEditableValue = montantInput.value;
                syncHidden();
            }
        });

        if (form) {
            form.addEventListener("submit", function (e) {
                /* Toujours synchroniser le hidden avant envoi */
                syncHidden();
                if (!validateMontantForSubmit()) {
                    e.preventDefault();
                    e.stopPropagation();
                }
            }, true);
        }

        applyPaiementMode(true);
    });
</script>

<%@ include file="../common/footer.jspf" %>
