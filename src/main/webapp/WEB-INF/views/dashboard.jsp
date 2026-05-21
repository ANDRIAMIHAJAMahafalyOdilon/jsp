<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ include file="common/header.jspf" %>

<style>
    .card-stats { border: none; border-radius: 12px; transition: transform .2s; }
    .card-stats:hover { transform: translateY(-5px); }
    .status-badge {
        font-size: .78rem;
        padding: 5px 12px;
        border-radius: 20px;
        font-weight: 600;
        white-space: nowrap;
        display: inline-block;
    }
    .paiement-cell { white-space: nowrap; }
    #newResModal .modal-content { max-height: 90vh; }
    #newResModal form { display: flex; flex-direction: column; height: 100%; }
    #newResModal .modal-body { overflow-y: auto; }
    #newResModal .modal-footer {
        position: sticky;
        bottom: 0;
        background: #fff;
        border-top: 1px solid #e5e7eb;
        z-index: 2;
    }
    .places-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(72px, 1fr));
        gap: 10px;
        max-width: 420px;
        margin: auto;
    }
    .driver-seat {
        grid-column: span 2;
        font-weight: 700;
        letter-spacing: .2px;
    }
</style>

<div class="row g-4 mb-4">
    <div class="col-md-4">
        <div class="card card-stats bg-white shadow-sm p-4 text-center">
            <p class="text-muted mb-1">Recette Totale</p>
            <h3 class="fw-bold text-primary mb-0"><fmt:formatNumber value="${totalRecette}" type="number" groupingUsed="true" /> Ar</h3>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card card-stats bg-white shadow-sm p-4 text-center">
            <p class="text-muted mb-1">Voyageurs</p>
            <h3 class="fw-bold text-success mb-0">${totalVoyageurs}</h3>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card card-stats bg-white shadow-sm p-4 text-center">
            <p class="text-muted mb-1">Reste à recouvrer</p>
            <h3 class="fw-bold text-danger mb-0"><fmt:formatNumber value="${resteAPayer}" type="number" groupingUsed="true" /> Ar</h3>
        </div>
    </div>
</div>

<div class="card border-0 shadow-sm rounded-4 mb-4">
    <div class="card-body p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0">Voyageurs par paiement</h5>
            <c:if test="${not empty payCat}">
                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/dashboard?idvoit=${selectedVoiture}">Voir tous les statuts</a>
            </c:if>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-4">
                <a class="text-decoration-none" href="${pageContext.request.contextPath}/dashboard?payCat=avance&idvoit=${selectedVoiture}">
                    <div class="card border-0 shadow-sm p-3 h-100">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-muted small">Avance + reste à payer</div>
                                <div class="fs-4 fw-bold text-warning">${countAvance}</div>
                            </div>
                            <span class="badge bg-warning-subtle text-warning">Avec avance</span>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-md-4">
                <a class="text-decoration-none" href="${pageContext.request.contextPath}/dashboard?payCat=non&idvoit=${selectedVoiture}">
                    <div class="card border-0 shadow-sm p-3 h-100">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-muted small">Pas encore payé</div>
                                <div class="fs-4 fw-bold text-danger">${countNon}</div>
                            </div>
                            <span class="badge bg-danger-subtle text-danger">Sans avance</span>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-md-4">
                <a class="text-decoration-none" href="${pageContext.request.contextPath}/dashboard?payCat=tout&idvoit=${selectedVoiture}">
                    <div class="card border-0 shadow-sm p-3 h-100">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-muted small">Tout payé</div>
                                <div class="fs-4 fw-bold text-success">${countTout}</div>
                            </div>
                            <span class="badge bg-success-subtle text-success">Tout payé</span>
                        </div>
                    </div>
                </a>
            </div>
        </div>

        <c:if test="${not empty payCat}">
            <div class="d-flex justify-content-between align-items-center mb-2 mt-2">
                <h6 class="fw-semibold mb-0">Liste des voyageurs — ${payCatLabel}</h6>
                <span class="badge text-bg-primary">Total : ${travellersCount}</span>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>Client</th>
                        <th>Contact</th>
                        <th>Voiture</th>
                        <th>Type</th>
                        <th>Place</th>
                        <th>Paiement</th>
                        <th>Avance</th>
                        <th>Reste</th>
                        <th>Date voyage</th>
                        <th class="text-center">Recu</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="t" items="${travellersByPaiement}">
                        <tr>
                            <td class="fw-semibold">${t.nomClient}</td>
                            <td>${t.numTel}</td>
                            <td>${t.idVoit}</td>
                            <td><span class="badge bg-secondary">${t.typeVoiture}</span></td>
                            <td>${t.place}</td>
                            <td class="paiement-cell">
                                <c:choose>
                                    <c:when test="${t.paiement == 'Tout payé' or fn:startsWith(t.paiement, 'Tout pay')}">
                                        <span class="status-badge bg-success-subtle text-success">Tout payé</span>
                                    </c:when>
                                    <c:when test="${t.paiement == 'Avec avance'}">
                                        <span class="status-badge bg-warning-subtle text-warning">Avec avance</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge bg-danger-subtle text-danger">Sans avance</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><fmt:formatNumber value="${t.montantAvance}" type="number" groupingUsed="true" /> Ar</td>
                            <td><fmt:formatNumber value="${t.reste}" type="number" groupingUsed="true" /> Ar</td>
                            <td>${t.dateVoyageLabel}</td>
                            <td class="text-center">
                                <a class="btn btn-light btn-sm" target="_blank" href="${pageContext.request.contextPath}/receipt?id=${t.idReserv}" title="PDF reçu">
                                    <i class="bi bi-filetype-pdf text-danger"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty travellersByPaiement}">
                        <tr><td colspan="10" class="text-center text-muted py-3">Aucun voyageur dans cette categorie.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </c:if>
        <c:if test="${empty payCat}">
            <p class="text-muted small mb-0 mt-2">Cliquez sur une carte ci-dessus pour afficher la liste des voyageurs et le nombre total par statut de paiement.</p>
        </c:if>

    </div>
</div>

<div class="card border-0 shadow-sm mt-3 page-block mb-4">
    <div class="card-body">
        <c:set var="showPlaces" value="${empty param.placesView || param.placesView == 'show'}" />
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="mb-0">Schema des places libres</h6>
            <form method="get" action="${pageContext.request.contextPath}/dashboard" class="d-flex gap-2">
                <select class="form-select form-select-sm" id="schemaVoitureSelect" name="idvoit">
                    <c:forEach var="v" items="${voitures}">
                        <option value="${v.idVoit}"
                                data-nbrplace="${v.nbrPlace}"
                                ${selectedVoiture == v.idVoit ? 'selected' : ''}>
                            ${v.idVoit} - ${v.type}
                        </option>
                    </c:forEach>
                </select>
                <button class="btn btn-sm ${showPlaces ? 'btn-primary' : 'btn-light border'}"
                        type="submit"
                        name="placesView"
                        value="${showPlaces ? 'hide' : 'show'}">
                    ${showPlaces ? 'Masquer' : 'Afficher'}
                </button>
            </form>
        </div>
        <c:if test="${showPlaces}">
            <div class="text-center mb-1">
                <div id="placesGrid" class="places-grid">
                    <c:forEach begin="1" end="${selectedVoitureNbrPlace}" var="p">
                        <c:choose>
                            <c:when test="${p == 1}">
                                <button class="btn rounded-3 driver-seat btn-outline-success"
                                        title="Chauffeur"
                                        type="button"
                                        style="min-height: 52px;">
                                    Chauffeur
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button class="btn rounded-3 btn-outline-success"
                                        title="Place ${p}"
                                        type="button"
                                        style="min-height: 52px;">
                                    <fmt:formatNumber value="${p}" minIntegerDigits="2" groupingUsed="false"/>
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <c:if test="${selectedVoitureNbrPlace <= 0}">
                        <div class="text-muted py-2">Aucune place libre pour cette voiture.</div>
                    </c:if>
                </div>
            </div>
        </c:if>
    </div>
</div>

<script>
    (function () {
        var select = document.getElementById("schemaVoitureSelect");
        var grid = document.getElementById("placesGrid");
        if (!select || !grid) return;

        function renderSchema(count, occupiedSet) {
            var total = Number(count) || 0;
            if (total <= 0) {
                grid.innerHTML = "<div class=\"text-muted py-2\">Aucune place libre pour cette voiture.</div>";
                return;
            }
            var html = "";
            occupiedSet = occupiedSet || new Set();
            for (var p = 1; p <= total; p++) {
                if (p === 1) {
                    html += "<button class=\"btn rounded-3 driver-seat btn-outline-dark\" type=\"button\" title=\"Chauffeur\" style=\"min-height: 52px;\">Chauffeur</button>";
                } else {
                    var n = String(p).padStart(2, "0");
                    var cls = occupiedSet.has(p) ? "btn-danger" : "btn-outline-success";
                    html += "<button class=\"btn rounded-3 " + cls + "\" type=\"button\" title=\"Place " + p + "\" style=\"min-height: 52px;\">" + n + "</button>";
                }
            }
            grid.innerHTML = html;
        }

        function loadAndRender() {
            var opt = select.options[select.selectedIndex];
            var total = Number(opt ? opt.getAttribute("data-nbrplace") : 0) || 0;
            var idVoit = opt ? opt.value : "";
            if (!idVoit || total <= 0) {
                renderSchema(0, new Set());
                return;
            }
            fetch("${pageContext.request.contextPath}/places?action=list&idvoit=" + encodeURIComponent(idVoit), {
                headers: { "Accept": "application/json" }
            })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    var freeSet = new Set((data && data.ok && Array.isArray(data.freePlaces) ? data.freePlaces : []).map(Number));
                    var occupiedSet = new Set();
                    for (var p = 1; p <= total; p++) {
                        if (!freeSet.has(p)) occupiedSet.add(p);
                    }
                    renderSchema(total, occupiedSet);
                })
                .catch(function () {
                    renderSchema(total, new Set());
                });
        }

        select.addEventListener("change", loadAndRender);
        // Keep first render aligned with DB for selected voiture.
        loadAndRender();
    })();
</script>


<%@ include file="common/footer.jspf" %>
