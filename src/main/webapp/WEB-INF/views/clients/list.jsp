<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="topbarSearch">
    <div class="d-flex align-items-center gap-2">
        <div class="live-search-wrap">
            <input id="clientsLiveSearch"
                   class="form-control form-control-sm pe-5"
                   name="search"
                   value="${search}"
                   placeholder="Nom ou telephone"
                   autocomplete="off" />
            <button id="clientsLiveSearchClear" class="live-search-clear" type="button" aria-label="Effacer recherche">×</button>
        </div>
    </div>
</c:set>
<%@ include file="../common/header.jspf" %>

<style>
    .joy-label { font-size: .86rem; font-weight: 600; color: #344054; margin-bottom: .4rem; }
    .joy-input {
        width: 100%; height: 44px; border: 1px solid #d0d7e2; border-radius: 12px;
        background: #fff; color: #1f2937; padding: .6rem .85rem; font-size: .95rem;
        transition: border-color .18s ease, box-shadow .18s ease;
    }
    .joy-input:focus { outline: none; border-color: #0d6efd; box-shadow: 0 0 0 4px rgba(13,110,253,.14); }
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
            data-bs-toggle="offcanvas" data-bs-target="#clientDrawer" aria-controls="clientDrawer">
        <i class="bi bi-plus-lg"></i>
        <span>Ajouter</span>
    </button>
</div>

<div class="card border-0 shadow-sm page-block">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="card-title mb-0">Liste des clients</h5>
            <span class="badge text-bg-light">${fn:length(clients)} enregistrements</span>
        </div>
        <div class="text-muted small mb-2"></div>
        <div class="table-responsive" style="min-height: 62vh;">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Nom</th>
                        <th>Telephone</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody id="clientsTableBody">
                    <c:forEach var="c" items="${clients}">
                        <tr>
                            <td>${c.idCli}</td>
                            <td>${c.nom}</td>
                            <td>${c.numTel}</td>
                            <td>
                                <a class="btn btn-sm btn-outline-warning" href="${pageContext.request.contextPath}/clients?action=edit&id=${c.idCli}" title="Modifier"
                                   data-confirm="Modifier ce client ?">
                                    <i class="bi bi-pencil"></i>
                                </a>
                                <a class="btn btn-sm btn-outline-danger" href="${pageContext.request.contextPath}/clients?action=delete&id=${c.idCli}" title="Supprimer"
                                   data-confirm="Supprimer ce client ?">
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
                        <a class="page-link" href="${pageContext.request.contextPath}/clients?page=${p}&search=${search}">${p}</a>
                    </li>
                </c:forEach>
            </ul>
        </nav>
    </div>
</div>

<div class="offcanvas offcanvas-end" tabindex="-1" id="clientDrawer" aria-labelledby="clientDrawerLabel">
    <div class="offcanvas-header border-bottom">
        <h5 class="offcanvas-title" id="clientDrawerLabel">${not empty clientEdit ? 'Modifier Client' : 'Ajouter Client'}</h5>
        <c:set var="cf" value="${not empty clientEdit ? clientEdit : clientForm}" />
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/clients" class="d-flex flex-column h-100">
        <div class="offcanvas-body">
            <input type="hidden" name="action" value="${not empty clientEdit ? 'update' : 'create'}" />
            <c:if test="${not empty clientEdit}">
                <input type="hidden" name="idcli" value="${clientEdit.idCli}" />
                <div class="mb-2">
                    <label class="joy-label">ID client</label>
                    <input class="joy-input" value="${clientEdit.idCli}" readonly />
                </div>
            </c:if>
            <c:if test="${empty clientEdit}">
                <div class="mb-2">
                    <label class="joy-label">ID client</label>
                    <input class="joy-input ${not empty idCliError ? 'is-invalid' : ''}"
                           id="clientIdInput"
                           name="idcli"
                           value="${cf.idCli}"
                           required
                           inputmode="numeric"
                           data-numeric-only="true"
                           oninput="this.value=this.value.replace(/\\D/g,'')"
                           placeholder="Ex: 12"
                           autocomplete="off"
                           data-manual-invalid="true" />
                    <div id="clientIdFeedback" class="invalid-feedback d-block" style="${empty idCliError ? 'display:none' : 'display:block'}">${idCliError}</div>
                </div>
            </c:if>
            <div class="mb-2">
                <label class="joy-label">Nom</label>
                <input class="joy-input"
                       name="nom"
                       value="${cf.nom}"
                       required
                       pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s'\-]{2,50}"
                       title="Nom: lettres uniquement (2 à 50 caractères)"
                       oninput="this.value=this.value.replace(/[^A-Za-zÀ-ÖØ-öø-ÿ\s'\-]/g,'');" />
            </div>
            <div class="mb-2">
                <label class="joy-label">Telephone</label>
                <input class="joy-input"
                       name="numtel"
                       value="${cf.numTel}"
                       required
                       inputmode="numeric"
                       pattern="[0-9]{6,20}"
                       title="Telephone: chiffres uniquement (6 à 20 chiffres)"
                       oninput="this.value=this.value.replace(/[^0-9]/g,'');" />
            </div>
        </div>
        <div class="offcanvas-footer border-top p-3 d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-light" data-bs-dismiss="offcanvas">Annuler</button>
            <button class="btn btn-primary" type="submit" data-confirm-submit="Confirmer l'enregistrement de ce client ?">Enregistrer</button>
        </div>
    </form>
</div>

<c:if test="${not empty clientEdit or openClientDrawer or not empty clientForm}">
    <script>
        window.addEventListener("DOMContentLoaded", function () {
            var drawerElement = document.getElementById("clientDrawer");
            if (drawerElement) {
                new bootstrap.Offcanvas(drawerElement).show();
            }
        });
    </script>
</c:if>

<script>
    (function () {
        var idInput = document.getElementById("clientIdInput");
        var feedback = document.getElementById("clientIdFeedback");
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
            fetch(ctx + "/clients?action=checkId&id=" + encodeURIComponent(id), { headers: { "Accept": "application/json" } })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    if (data && data.exists) {
                        setFeedback("Cet ID client existe deja.", true);
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
        var input = document.getElementById("clientsLiveSearch");
        var clearBtn = document.getElementById("clientsLiveSearchClear");
        var tbody = document.getElementById("clientsTableBody");
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
