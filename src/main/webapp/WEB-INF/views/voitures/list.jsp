<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="topbarSearch">
    <div class="live-search-wrap">
        <input id="voituresLiveSearch"
               class="form-control form-control-sm pe-5"
               placeholder="Rechercher voiture..."
               autocomplete="off" />
        <button id="voituresLiveSearchClear" class="live-search-clear" type="button" aria-label="Effacer recherche">×</button>
    </div>
</c:set>
<%@ include file="../common/header.jspf" %>

<style>
    .joy-label {
        font-size: 0.86rem;
        font-weight: 600;
        color: #344054;
        margin-bottom: 0.4rem;
    }
    .joy-input {
        width: 100%;
        height: 44px;
        border: 1px solid #d0d7e2;
        border-radius: 12px;
        background: #fdfdff;
        color: #1f2937;
        padding: 0.6rem 0.85rem;
        font-size: 0.95rem;
        transition: border-color .18s ease, box-shadow .18s ease, background-color .18s ease;
    }
    .joy-input::placeholder {
        color: #98a2b3;
    }
    .joy-input:hover {
        border-color: #b8c2d1;
        background: #ffffff;
    }
    .joy-input:focus {
        outline: none;
        border-color: #0d6efd;
        background: #ffffff;
        box-shadow: 0 0 0 4px rgba(13, 110, 253, 0.14);
    }
    .joy-input[readonly] {
        background: #f2f4f7;
        color: #667085;
        cursor: not-allowed;
    }
    .live-search-wrap { position: relative; min-width: 260px; }
    .live-search-clear {
        position: absolute;
        right: 6px;
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

    /* Force prevent any horizontal scroll for the topbar + search */
    html, body { overflow-x: hidden !important; }
    .topbar { overflow-x: hidden !important; }
    .app-shell { overflow-x: hidden !important; }
</style>

<div class="d-flex justify-content-between align-items-center mb-3">
    <button class="btn btn-primary rounded-3 d-flex align-items-center gap-2"
            data-bs-toggle="offcanvas" data-bs-target="#voitureDrawer" aria-controls="voitureDrawer">
        <i class="bi bi-plus-lg"></i>
        <span>Ajouter</span>
    </button>
</div>

<div class="card border-0 shadow-sm page-block">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="card-title mb-0">Liste des voitures</h5>
            <div class="d-flex align-items-center gap-2">
                <span class="badge text-bg-light">${fn:length(voitures)} enregistrements</span>
            </div>
        </div>
        <div class="table-responsive" style="min-height: 62vh;">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Design</th>
                        <th>Type</th>
                        <th>Places</th>
                        <th>Frais</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody id="voituresTableBody">
                    <c:forEach var="v" items="${voitures}">
                        <tr>
                            <td>${v.idVoit}</td>
                            <td>${v.design}</td>
                            <td>${v.type}</td>
                            <td>${v.nbrPlace}</td>
                            <td><fmt:formatNumber value="${v.frais}" type="number" groupingUsed="true" /> Ar</td>
                            <td>
                                <a class="btn btn-sm btn-outline-warning" href="${pageContext.request.contextPath}/voitures?action=edit&id=${v.idVoit}" title="Modifier"
                                   data-confirm="Modifier cette voiture ?">
                                    <i class="bi bi-pencil"></i>
                                </a>
                                <a class="btn btn-sm btn-outline-danger" href="${pageContext.request.contextPath}/voitures?action=delete&id=${v.idVoit}" title="Supprimer"
                                   data-confirm="Supprimer cette voiture ?">
                                    <i class="bi bi-trash"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
        </div>
        <nav class="mt-3">
            <ul class="pagination pagination-sm justify-content-end mb-0">
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/voitures?page=${p}">${p}</a>
                    </li>
                </c:forEach>
            </ul>
        </nav>
    </div>
</div>

<div class="offcanvas offcanvas-end" tabindex="-1" id="voitureDrawer" aria-labelledby="voitureDrawerLabel">
    <div class="offcanvas-header border-bottom">
        <h5 class="offcanvas-title" id="voitureDrawerLabel">${not empty voitureEdit ? 'Modifier Voiture' : 'Ajouter Voiture'}</h5>
        <c:set var="vf" value="${not empty voitureEdit ? voitureEdit : voitureForm}" />
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/voitures" class="d-flex flex-column h-100">
        <div class="offcanvas-body">
            <input type="hidden" name="action" value="${not empty voitureEdit ? 'update' : 'create'}" />
            <c:if test="${not empty voitureEdit}">
                <input type="hidden" name="idvoit" value="${voitureEdit.idVoit}" />
                <div class="mb-2">
                    <label class="joy-label">ID voiture</label>
                    <input class="joy-input" value="${voitureEdit.idVoit}" readonly />
                </div>
            </c:if>
            <c:if test="${empty voitureEdit}">
                <div class="mb-2">
                    <label class="joy-label">ID voiture</label>
                    <input class="joy-input ${not empty idVoitError ? 'is-invalid' : ''}"
                           id="voitureIdInput"
                           name="idvoit"
                           value="${vf.idVoit}"
                           placeholder="Ex: RES-5 ou 5"
                           required
                           autocomplete="off"
                           data-manual-invalid="true" />
                    <div id="voitureIdFeedback" class="invalid-feedback d-block" style="${empty idVoitError ? 'display:none' : 'display:block'}">${idVoitError}</div>
                </div>
            </c:if>
            <div class="mb-2">
                <label class="joy-label">Design</label>
                <input class="joy-input" name="design" value="${vf.design}" required />
            </div>
            <div class="mb-2">
                <label class="joy-label">Type</label>
                <select class="joy-input" name="type" required>
                    <option value="Simple" ${vf.type == 'Simple' ? 'selected' : ''}>Simple</option>
                    <option value="Premium" ${vf.type == 'Premium' ? 'selected' : ''}>Premium</option>
                    <option value="VIP" ${vf.type == 'VIP' ? 'selected' : ''}>VIP</option>
                </select>
            </div>
            <div class="mb-2">
                <label class="joy-label">Nombre de places</label>
                <input class="joy-input"
                       type="text"
                       inputmode="numeric"
                       data-numeric-only="true"
                       oninput="this.value=this.value.replace(/\\D/g,'')"
                       name="nbrplace"
                       value="${vf.nbrPlace}"
                       required />
            </div>
            <div class="mb-1">
                <label class="joy-label">Frais</label>
                <input class="joy-input"
                       type="text"
                       inputmode="numeric"
                       data-numeric-only="true"
                       oninput="this.value=this.value.replace(/\\D/g,'')"
                       name="frais"
                       value="${vf.frais}"
                       required />
            </div>
        </div>
        <div class="offcanvas-footer border-top p-3 d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-light" data-bs-dismiss="offcanvas">Annuler</button>
            <button class="btn btn-primary" type="submit" data-confirm-submit="Confirmer l'enregistrement de cette voiture ?">Enregistrer</button>
        </div>
    </form>
</div>

<c:if test="${not empty voitureEdit or openVoitureDrawer or not empty voitureForm}">
    <script>
        window.addEventListener("DOMContentLoaded", function () {
            var drawerElement = document.getElementById("voitureDrawer");
            if (drawerElement) {
                new bootstrap.Offcanvas(drawerElement).show();
            }
        });
    </script>
</c:if>

<script>
    (function () {
        var idInput = document.getElementById("voitureIdInput");
        var feedback = document.getElementById("voitureIdFeedback");
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
            fetch(ctx + "/voitures?action=checkId&id=" + encodeURIComponent(id), { headers: { "Accept": "application/json" } })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    if (data && data.exists) {
                        setFeedback("Cet ID voiture existe deja.", true);
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
        var input = document.getElementById("voituresLiveSearch");
        var clearBtn = document.getElementById("voituresLiveSearchClear");
        var tbody = document.getElementById("voituresTableBody");
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

<%@ include file="../common/footer.jspf" %>
