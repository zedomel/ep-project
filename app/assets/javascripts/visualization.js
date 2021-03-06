var margin = {top: 100, right: 100, bottom: 100, left: 100},
padding = 3, // separation between same-color nodes
clusterPadding = 6, // separation between different-color nodes
fixRadius = 5;

//Inicializa visualização
$(function(){

	$("#reset-btn").prop('disabled', true);
	$("#step-btn").prop('disabled', true);
	$("#reheat-btn").prop('disabled', true);
	$("#show-list-btn").prop('disabled', true);
	$("#zoom-btn").prop('disabled', true);
	$("#download-btn").prop('disabled', true);

	$("#reset-btn").click(resetVisualization);
	$("#zoom-btn").click(zoomTool);
	$("#download-btn").click(downloadDocuments);

//	$("#zoom-btn").on( "mouseleave", function(){
//	$(this).tooltip('hide');
//	} );
	$("#show-list-btn").on( "mouseleave", function(){
		$(this).tooltip('hide');
	} );

	$("#step-btn").click(nextVisualizationStep);
	$("#reheat-btn").click(reheat);
	$("#show-list-btn").click(showListTool);

	// Tooltip on mouse over
	var tip = d3.select("body").append("div")
	.attr("class", "node-tooltip")
	.style("opacity", 0);

});

//Criar uma visualização em uma nova aba
createVisualization = function(jsonData){

	var svg;
//	var n = jsonData.documents.length; // no. total de documentos
//	if ( n === 0 ){
//	noDocumentsFound();
//	return;
//	}

	// Esconde loading...
	$('#loading').addClass('hidden');

	// Verifica se há pontos a serem exibidos
	if ( selectedTab !== null && jsonData.op === "zoom" ){
		var parentDocs = selectedTab.documents;
		jsonData.documents = jsonData.documents.filter(function(element, index, array){
			return parentDocs[ element.id ] === undefined;
		});
	}

	// Se não houver nenhum documento, 
	// não há nada que desenhar
	var n = jsonData.documents.length;
	if ( n === 0 ){
		noDocumentsFound();
		var isZoomActive = $('#zoom-btn').hasClass('active');
		if ( isZoomActive ){
			svg = d3.select("#" + selectedTabId + " svg.visualization");
			activeZoom(svg);
		}
		return;
	}

	// Cria nova aba
	var	currentTab = addNewTab(jsonData.op);
	currentTab.step = 1;

	$("#reset-btn").prop('disabled', false);
	$("#step-btn").prop('disabled', false);
	$("#show-list-btn").prop('disabled', false);
	$("#zoom-btn").prop('disabled', false);
	$("#download-btn").prop('disabled', false);

	svg = d3.select("#" + currentTab.id + " svg.visualization");
	var width = $("#" + currentTab.id + " svg.visualization").width(),
	height = width * 6 / 16; //$("#" + currentTab.id + " svg").height();

	$("#" + currentTab.id + " svg.visualization").height(height);
	$(".tab-content").height(height);

	svg.attr('width', width);
	svg.attr('height', height);

	svg.on('click', function(){
		d3.select('.fixed-tooltip')
		.transition()
		.duration(500)
		.style('opacity', 0)
		.style('display', 'none')
		.remove();
	});

	var m = jsonData.nclusters; // no. de clusters

	var maxDocs = $("#max-number-of-docs").val();
	var isColoringByRelevance = $('#color-schema').prop('checked');
	var minRank, maxRank, minRadius, maxRadius;
	
	maxRank = jsonData.documents[0].rank;
	minRank = jsonData.documents[n-1].rank;
	maxRadius = width * jsonData.maxRadiusPerc;
	minRadius = width * jsonData.minRadiusPerc;

	// Radius interpolator based on document relevance
	currentTab.radiusInterpolator = d3.scaleLinear()
		.domain([minRank, maxRank])
		.range([minRadius, maxRadius])
		.interpolate(d3.interpolateRound);
	
	// Carrega documentos para a aba atual
	var index = currentTab.loadData(jsonData, width * height, maxDocs);

	// Valores min/max das coordenadas x,y 
	// dos documentos atuais
	var minMaxX, minMaxY;
	if ( jsonData.op === "zoom"){
		minMaxX = [jsonData.bounds[0], jsonData.bounds[2]];
		minMaxY = [jsonData.bounds[1], jsonData.bounds[3]];
	}
	else{
		minMaxX = d3.extent(jsonData.documents,function(d){ return d.x; });
		minMaxY = d3.extent(jsonData.documents,function(d){ return d.y; });
	}
	
	var densityMap = jsonData.densityMap;

	// Inicializa transformações 2D (x,y) 
	// do intervalo [-1,1] para [0, width/height]
	currentTab.xy(width, height, minMaxX[0], minMaxX[1], minMaxY[0],minMaxY[1]);
	currentTab.xy_inverse(width, height);

	// Criar contornos
	var contours;
	var gridSize = [256,256];
	if ( densityMap === 1 ){
		contours = d3.contourDensity()
			.x(function(d){ return selectedTab.x(d.x);})
			.y(function(d){ return selectedTab.y(d.y);})
			.size([width,height])
			.bandwidth(10);
	}
	else{
		var thresholds = d3.range(0,5)
		.map(function(p){ return Math.pow(2,p);});
	
		gridSize = jsonData.gridSize;
		contours = d3.contours()
			.size(gridSize)
			.thresholds(thresholds);
	}
	contours = contours(currentTab.densities);

	// Inicializa coloração dos clusters
	if ( isColoringByRelevance ){
		currentTab.initColorSchema(true, minRank, maxRank);
	}
	else{
		currentTab.initColorSchema(false, 0, m);
	}
	jsonData = null;

	// Coloração dos contornos
	var contourColor = d3.scaleSequential(d3.interpolateOranges)
	.domain(d3.extent(contours.map(function(p) { return p.value;}))); // Points per square pixel.
	
	// Array para armazenar o maior nó de cada cluster (centroide)
	currentTab.clusters = new Array(m);

	//Cria nós
	currentTab.nodes = createNodes(currentTab.documents);

	// Se pontos do contorno devem ser mostrados na
	// visualizaçã, cria pontos.
	var showPoints = $("#show-density-points").prop('checked');
	if ( showPoints ){
		currentTab.points = createPoints(currentTab.densities);
	}
	currentTab.densities = null;

	// Marcadores dos links (setas)
	var defs = svg.append("defs");

	defs.selectAll("marker")
	.data(["link" + currentTab.id])
	.enter().append("marker")
	.attr("id", function(d) { return d; })
	.attr("viewBox", "0 -5 10 10")
	.attr("refX", 8)
	.attr("refY", 0)
	.attr("markerWidth", 6)
	.attr("markerHeight", 6)
	.attr("orient", "auto")
	.append("path")
	.attr("d", "M0,-5L10,0L0,5");

	// Contornos
	svg.insert("g", "g")
	.attr("fill", "none")
//	.attr("stroke", "#000")
//	.attr("stroke-width", 0.5)
//	.attr("stroke-linejoin", "round")
	.selectAll("path")
	.data(contours)
	.enter().append("path")
	.attr("d", densityMap === 1 ? d3.geoPath() : d3.geoPath(d3.geoIdentity()
			.scale(width/gridSize[0])
//			.translate([0,-height/2])
			))
	.attr("fill", function(d){ return contourColor(d.value);});

	contours = null;

	var g = svg.append("g");

	// Circulos
	currentTab.circles = g
	.datum(currentTab.nodes)
	.selectAll('.circle')
	.data(function(d) { return d;})
	.enter().append('circle')
	.attr('r', function(d) { return fixRadius; })
	.attr('cx', function(d) { return d.x;})
	.attr('cy', function(d) { return  d.y;})
	.attr('fill', function(d) { return  selectedTab.coloring(d);})
	.attr('stroke', 'black')
	.attr('stroke-width', 1)
	.attr('fill-opacity', 0.80)
	.on("mouseover", showTip)
	.on("mouseout", hideTip)
	.on("click", toggleLinks);	

	// Se há pontos de densidade, 
	// então desenha na visualização.
	var point;
	if ( currentTab.points !== null ){
		point = g
		.datum(currentTab.points)
		.selectAll('.circle')
		.data(function(d) { return d;})
		.enter().append('circle')
		.attr('r', function(d) { return d.r; })
		.attr('cx', function(d) { return d.x;})
		.attr('cy', function(d) { return  d.y;})
		.attr('fill', function(d) { return  selectedTab.coloring(d);})
		.attr('stroke', 'black')
		.attr('stroke-width', 1);
	}

	// Ferramenta de seleção (escondida por enquanto).
	var selection = svg.append("path")
	.attr("class", "selection")
	.attr("visibility", "hidden");

	var isActive = $("#zoom-btn").hasClass("active");
	if ( isActive )
		activeZoom( svg );
	else
		desactiveZoom ( svg );

	if ( currentTab.parentId === null ){
		createMiniMap(svg, currentTab);
	}
	else{
		copyMiniMapFromParent(svg, currentTab, minMaxX[0], minMaxY[0], minMaxX[1], minMaxY[1]);
	}

	// Exibe/Esconde circulos?
	var showCircles = $('#show-circles-btn').hasClass('glyphicon-eye-close');
	if ( showCircles ){
		d3.selectAll('circle')
		.style('display', null);
	}
	else{
		d3.selectAll('circle')
		.style('display', 'none');
	}


	// Adiciona legenda
	currentTab.rankFactor = 1.0/minRank;
	
	var slider = $("#" + currentTab.id + " .slider-range .slider");
	$(slider).slider({
		tooltip:  "always",
		min: (minRank * currentTab.rankFactor),
		max: Math.ceil(maxRank * currentTab.rankFactor),
		precision: 3,
		value: [(minRank * currentTab.rankFactor ), Math.ceil(maxRank * currentTab.rankFactor)]
	}).on('slide', sliderRankChange);

	var colors = currentTab.color.range();
	var gradientStr = colors[0];
	for(var i = 1; i < colors.length; i++){
		gradientStr += "," + colors[i];
	}

	$("#" + currentTab.id + " .slider-range .slider .slider-track").css({
		"background": "linear-gradient(to right," + gradientStr + ")"
	});

	// ###### Fim do primeiro passo: criar visualização ######
};

//Segundo passo: atribui raios ao circulos
function secondStep(){
	selectedTab.circles
	.transition()
	.duration(750)
	.attr('r', function(d) { return d.r; })
	.attr('fill-opacity', function(d) { return 0.3; });
}

//Terceiro passo: inicializa esquema de forças
function thirdStep(){

	// Configuração de forças
	var collideStrength = parseFloat( $("#collision-force").val() ),
	manyBodyStrength = parseFloat($("#manybody-force").val()),
	clusteringForceOn = $("#clustering-force").prop('checked');

	// Força de colisão
	var forceCollide = d3.forceCollide()
	.radius(function(d) { return d.r + padding; }) // raio do circulo + padding
	.strength(collideStrength)
	.iterations(1); //somente 1 iteração (economia de memoria).

	// Força de atração
	// Evita dispersão
	var forceGravity = d3.forceManyBody()
	.strength( manyBodyStrength );

	// Inicializa simulação
	selectedTab.simulation = d3.forceSimulation(selectedTab.nodes)
	.force("collide", forceCollide)
	.force("gravity", forceGravity)
	.on("tick", ticked)
	.on("end", endSimulation);

	// Força entre links (citações)
	// Circulos com alguma ligação serão posicionados 
	// próximos caso strength != 0.
	// Default strenght == 0 (somente exibe links/setas)
	if ( selectedTab.links !== null ){
		var forceLink = d3.forceLink()
		.id(function(d) { return d.id; })
		.links(selectedTab.links)
		.strength(0)
		.distance(0);

		selectedTab.simulation
		.force("link", forceLink);
	}

	// Se a força de clusterização estiver selecionada,
	// ativa a força na simulação.
	if ( clusteringForceOn )
		selectedTab.simulation.force("cluster", forceCluster());
}

/** Função para enviar coordenadas da area selecionada
 * pela ferramenta de zoom
 * @param start posicao x,y inicial
 * @param end posicao x,y final
 * @returns void
 **/
function selectArea(p){

	if ( tabs.length >= maxNumberOfTabs ){
		showMaxTabsAlert();
		return;
	}

	var numClusters = $("#num-clusters").val();
	var r = jsRoutes.controllers.HomeController.zoom(),
	selectionWidth = $(".selection")[0].style.width.replace("px", ""),
	selectionHeight = $(".selection")[0].style.height.replace("px",""),
	maxDocs = $("#max-number-of-docs").val();
	
	// Transforma coordenada para espaço original
	// no intervalo [-1,1]
	var start = [ selectedTab.x_inv(p[0] - selectionWidth/2 ) , selectedTab.y_inv(p[1] - selectionHeight/2) ],
	end = [ selectedTab.x_inv(p[0] + selectionWidth/2), selectedTab.y_inv(p[1] + selectionHeight/2)];
	$.ajax({url: r.url, type: r.type, data: {
		start: start,
		end: end,
		numClusters: numClusters,
		maxDocs: maxDocs
	},
	success: createVisualization, error: errorFn, dataType: "json"});
}

/** Finaliza simulação.
 * Ativa botao de reheating e habilita tooltips.
 * 
 * @returns void
 */
function endSimulation(){
	selectedTab.circles
	.attr("fill-opacity", 0.80);

	$("#reheat-btn").prop('disabled', false);
}

/** Executa a cada iteracão da simulação.
 * Corrigi posição cx,cy dos circulos evitando
 * que estes saiam fora da area de visualização
 * (bounding box).
 *  
 * @returns void
 */
function ticked(){

	var width = $('#' + selectedTab.id + " svg.visualization").width();
	var height = $('#' + selectedTab.id + " svg.visualization").height();

	selectedTab.circles
	.attr("cx", function(d) { return (d.x = Math.max(d.r, Math.min(width - d.r, d.x)));})
	.attr("cy", function(d) { return (d.y = Math.max(d.r, Math.min(height - d.r, d.y)));});

	if ( selectedTab.path !== null ){
		selectedTab.path
		.attr("d", linkArc);
	}
}