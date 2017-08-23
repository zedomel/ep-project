/*
 * Funções para manipulação das abas.
 */

var tabCount, selectedTab, tabs;

/**
 * Inicializa variavies globais
 */
$(function(){
	tabs = [];
	tabCount = 0;
	selectedTabIndex = null;
});

/**
 * Adiciona nova aba, criando um novo
 * objeto Tab.
 * @param op tipo de operação (zoom ou search).
 * @returns nova aba (objeto Tab).
 */
function addNewTab(op){
	var zoomLevel, newTab;

	//Contador de abas
	++tabCount;

	if ( op === 'search' ){
		zoomLevel = 0;
		newTab = addTab('main-'+tabCount, 'Visualization ID: ' + tabCount, null);
	}else{ // op === 'zoom'
		zoomLevel = selectedTab.zoomLevel + 1;
		newTab = addTab('zoom-'+tabCount, 'Zoom Level: ' + zoomLevel, selectedTab.id);
	}

	// Zoom Level
	newTab.zoomLevel = zoomLevel;

	return newTab;
}

/**
 * Funcão auxiliar para criar nó DOM 
 * correspondente a nova aba.
 * @param id id do objeto DOM a ser criado.
 * @param title título da nova aba.
 * @returns nova aba (objecto Tab).
 */
function addTab(id, title, parentId){
	// Criar um link para a nova aba no menu de abas
	$('#viz-tabs').append('<li role="presentation" class="">' + 
			'<a href="#' + id + '" aria-controls="' + id + '" role="tab"' +  
			'data-toggle="tab" data-tab-index="' + tabCount + '" id="tab-' + id + '">' + title + 
	' <button class="close"><span aria-hidden="true" class="glyphicon glyphicon-remove"></span></button></a></li>');
	$('.tab-content').append(newTabContent(id));

	// Adiciona tab ao vetor e marca a nova tab
	// como selecionada
	tabs[id] = new Tab(id, parentId);
	selectedTab = tabs[id];


	// Trata evento de click
	$('#tab-' + id).click(function (e) {
		e.preventDefault();
		$(this).tab('show');
	});

	// Se o click for no ícone para fechar a aba (X)
	$('#tab-'+id + ' .close').click(function(e){
		// Pega id da aba
		var tabId = $(this).parent().attr('aria-controls');
		// Remove conteúdo da aba
		$('#' +tabId).remove();
		// Seleciona aba anteiror ou posterior,
		// caso existir e muda visualização
		// para esta aba.
		var prevTab = $(this).parents('li').prev();
		if ( !prevTab.length )
			prevTab = $(this).parents('li').next();
		// Se não houver mais abas
		// desabilita botões e marca 
		// aba selecionada como null.
		if ( !prevTab.length ){
			selectedTab = null;
			$("#step-btn").prop('disabled', true);
			$("#reheat-btn").prop('disabled', true);
			$("#zoom-btn").prop('disabled', true);
			$("#show-list-btn").prop('disabled', true);
		}

		// Remove seletor da aba e respectivo
		// objeto Tab.
		$(this).parents('li').remove();
		deleteTab(tabId);
		--tabCount;

		// Se ainda existir outra aba
		// exibe a aba e marca a aba selecionada
		if ( prevTab.length ){
			$(prevTab).children('a').tab('show');
			selectedTabId = $(prevTab).children('a').attr('aria-controls');
			selectedTab = tabs[selectedTabId];
		}
	});

	// Eventos de troca de aba
	$('#tab-'+id).on('show.bs.tab', function(e){
		selectedTabId = $(e.target).attr('aria-controls');
		selectedTab = tabs[selectedTabId];
		$("#step-btn").prop('disabled', selectedTab.step === 3);
		$("#reheat-btn").prop('disabled', selectedTab.step !== 3);
		$(".visualization-wrapper")
		.toggleClass("zoom-cursor", $("#zoom-btn").hasClass("active"));

		var isZoomActive = $('#zoom-btn').hasClass('active');
		if ( isZoomActive ){
			var svg = d3.select("#" + selectedTabId + " svg");
			svg.on('mousemove', function(){
				var start = d3.mouse(this);
				startSelection(start, svg);
			})
			.on("click.selection", function() {
				d3.select(this).on("mousemove", null);
				//			$("#zoom-btn").click();
				endSelection(d3.mouse(this));
			})
			.on("wheel", function(){
				var delta = d3.event.deltaY,
				newWidth, newHeight;

				var selectionWidth = $(".selection").width(),
				svgWidth = svg.attr('width');

				if ( delta > 0 ){
					newWidth = Math.min(svgWidth, selectionWidth * 1.1);
					newHeight = newWidth * 6 / 16;
				}
				else{
					newWidth = Math.max(200, selectionWidth * 0.9);
					newHeight = newWidth * 6 / 16;
				}

				$(".selection").width(newWidth);
				$(".selection").height(newHeight);

				var start = d3.mouse(this);
				svg.select('.selection')
				.attr("d", rect(start[0], start[1], newWidth, newHeight));
			});

			svg.select('.selection')
			.attr("visibility", "visible");
		}
	})
	.on('hide.bs.tab', function(e){
		var prevTabId = $(e.target).attr('aria-controls');
		d3.select("#" + prevTabId + " svg")
		.on('mousemove', null)
		.on('click.selection', null)
		.select('.selection')
		.attr("visibility", "hidden");

	});

	// Exibe nova aba adicionada
	$('#tab-'+ id).tab('show');

	return tabs[id];
}

/**
 * Delete um objeto aba e libera memoria.
 * @param tabId id do nó DOM da aba.
 * @returns void
 */
function deleteTab(tabId){
	tabs[tabId].deleteTab();
	delete tabs[tabId];
}

/**
 * Criar conteúdo inicial para a nova aba.
 * @param id id do nó DOM da nova aba
 * @returns conteúdo HTML da nova aba.
 */
function newTabContent(id){
	return '<div role="tabpanel" class="tab-pane" id="' + id + '">' + 
	'<div class="visualization-wrapper">' + 
	'<svg id="visualization" class="visualization"></svg></div>' +
	'<div class="documents-list_wrapper">' +
	'<div class="documents-table">' +
	'<table class="table table-hover table-striped hidden">' +
	'<thead><tr><th class="doc-index"></th><th class="doc-title">Title</th><th class="doc-authors">Authors</th>' +
	'<th class="doc-year">Pub. Year</th><th class="doc-doi">DOI</th><th class="doc-relevance">Relevance</th>' +
	'<th class="doc-cluster">Cluster</th></tr></thead><tbody></tbody></table></div></div></div>';
}