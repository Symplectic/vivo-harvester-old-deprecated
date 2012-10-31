$(function(){
	jquery_sticky_footer();
	content_move_product_page();
	add_br_into_timeline_h1();
	block_quote_close_icon();

	if(typeof(window.matchMedia) == "undefined"){
		// Crap browser support. Don't do anything fancy and treat it as a desktop browser regardless.
		dropdown_menu_desktop();
		carousel_news_homepage();
		background_reload_homepage();
	}else{
		// Good browser support.
		Harvey.attach('screen and (max-width:480px)', {
			setup: function(){ // called when the query becomes valid for the first time

			},
			on: function(){  // called each time the query is activated
				dropdown_menu_mobile();
			},
			off: function(){ // called each time the query is deactivated
				$('#nav li.dropdown > a').unbind('click');
			} 
		});

		Harvey.attach('screen and (min-width:481px)', {
			setup: function(){ // called when the query becomes valid for the first time		
				dropdown_menu_desktop();
				carousel_news_homepage();
				background_reload_homepage();
			}, 
			on: function(){ // called each time the query is activated

			}, 
			off: function(){// called each time the query is deactivated 
				$('#nav').unbind('hover');
				$('#header #nav li ul.dropdown').css('min-height', 0);
			} 
		});
	}

});

$(window).bind('resize scroll', jquery_sticky_footer);

// jQuery sticky footer
function jquery_sticky_footer() {
	var mFoo = $("#footer");

	if ((($(document.body).height() + mFoo.height()) < $(window).height() && mFoo.css("position") == "fixed") || ($(document.body).height() < $(window).height() && mFoo.css("position") != "fixed")) {
		mFoo.css({
			position: "fixed",
			bottom: "0"
		});
	} else {
		mFoo.css({
			position: "static"
		});
	}
}

// Layout tweaks for transfoming non-full-width content to full-width
function content_move_product_page() {
	$('#product-extra-boxes').insertAfter('#content');
};

// Add <br> into Timeline h1 to make it look nicer
function add_br_into_timeline_h1() {
	$('body.timeline .page-title h1').each(function() {
		var getContent=$(this).text();
		var newString=getContent.replace('The History of Symplectic','The<br> History of Symplectic');
		$(this).html(newString);
	});
};

// Change homepage background image on reload
function background_reload_homepage() {
	var imageTank = ['home_bg_1.jpg'];
	$('body.home').css({'background-image': 'url(/assets/themes/symplectic/home/' + imageTank[Math.floor(Math.random() * imageTank.length)] + ')'});
	$('<div id="home-blue-line"></div>').appendTo('body.home #content');
}

// Carousel news on the homepage
function carousel_news_homepage() {
    $('#home-news-slides').jCarouselLite({
        btnNext: '#home-news-slides-nav .next',
        btnPrev: '#home-news-slides-nav .prev',
        circular: 1,
        vertical: 0,
        visible: 1,
		easing: 'easeInQuart',
		speed: 1000
    });
	$('#home-news-slides-nav .arrow').show();
};

// Dropdown for menu on desktops
function dropdown_menu_desktop() {
	$('#nav li.dropdown').hover(function() {
		$('ul.dropdown').show();
	}, function() {
		$('ul.dropdown').hide();
	});
	var max_dropdown_height = $('#header #nav li ul.dropdown').height();
	$('#header #nav li ul.dropdown').css('min-height', max_dropdown_height + 0);
};

// Dropdown for menu on mobile
function dropdown_menu_mobile() {
	$('#nav li.dropdown > a').click(function() {
		var currentStatus = $(this).siblings('ul.dropdown').css("display");
		$('ul.dropdown').hide();
		if(currentStatus != "block"){
			$(this).siblings('ul.dropdown').slideDown('fast');
		}
		
		return false;
	});
};

// Add <span> after blockquote element
function block_quote_close_icon() {
	$('<span class="close-icon">&nbsp;</span>').appendTo('blockquote p');
};
