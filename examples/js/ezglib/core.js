// Compiled by ClojureScript 0.0-2173
goog.provide('ezglib.core');
goog.require('cljs.core');
/**
* Creates a new game state. A game state has two components:
* an update function and a render function. Both functions are called
* once a frame.
*/
ezglib.core.state = (function state(update,render){return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [update,render], null);
});
/**
* Adds a state to the game.
*/
ezglib.core.add_state_BANG_ = (function add_state_BANG_(game,state_id,state){return cljs.core.swap_BANG_.call(null,new cljs.core.Keyword(null,"states","states",4416389492).cljs$core$IFn$_invoke$arity$1(game),cljs.core.assoc,state_id,state);
});
/**
* Removes a state from the game.
*/
ezglib.core.remove_state_BANG_ = (function remove_state_BANG_(game,state_id){return cljs.core.swap_BANG_.call(null,new cljs.core.Keyword(null,"states","states",4416389492).cljs$core$IFn$_invoke$arity$1(game),cljs.core.dissoc,state_id);
});
/**
* Gets the current state of the game.
*/
ezglib.core.current_state = (function current_state(game){return cljs.core.deref.call(null,new cljs.core.Keyword(null,"state","state",1123661827).cljs$core$IFn$_invoke$arity$1(game));
});
/**
* Gets alll availble states of the game.
*/
ezglib.core.states = (function states(game){return cljs.core.deref.call(null,new cljs.core.Keyword(null,"states","states",4416389492).cljs$core$IFn$_invoke$arity$1(game));
});
ezglib.core.default_state = ezglib.core.state.call(null,(function (game){return null;
}),(function (game){return null;
}));
/**
* Makes an ezglib game. element-id is the DOM
* element in which the game is injected. game-id is
* the id of the game element.
*/
ezglib.core.game = (function game(width,height,element_id,game_id){var e = document.getElementById(element_id);var c = document.createElement("canvas");c.id = game_id;
c.width = width;
c.height = height;
e.appendChild(c);
return new cljs.core.PersistentArrayMap(null, 7, [new cljs.core.Keyword(null,"width","width",1127031096),width,new cljs.core.Keyword(null,"height","height",4087841945),height,new cljs.core.Keyword(null,"states","states",4416389492),cljs.core.atom.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"default","default",2558708147),ezglib.core.default_state], null)),new cljs.core.Keyword(null,"state","state",1123661827),cljs.core.atom.call(null,new cljs.core.Keyword(null,"default","default",2558708147)),new cljs.core.Keyword(null,"element","element",3646034542),e,new cljs.core.Keyword(null,"loop","loop",1017231894),cljs.core.atom.call(null,true),new cljs.core.Keyword(null,"canvas","canvas",3941165258),c], null);
});
ezglib.core.game_loop = (function game_loop(game,state_id,callback_caller){cljs.core.reset_BANG_.call(null,new cljs.core.Keyword(null,"state","state",1123661827).cljs$core$IFn$_invoke$arity$1(game),state_id);
cljs.core.reset_BANG_.call(null,new cljs.core.Keyword(null,"loop","loop",1017231894).cljs$core$IFn$_invoke$arity$1(game),true);
return (function cb(){if(cljs.core.truth_(cljs.core.deref.call(null,new cljs.core.Keyword(null,"loop","loop",1017231894).cljs$core$IFn$_invoke$arity$1(game))))
{callback_caller.call(null,cb);
} else
{}
var vec__4796 = cljs.core.deref.call(null,new cljs.core.Keyword(null,"states","states",4416389492).cljs$core$IFn$_invoke$arity$1(game)).call(null,cljs.core.deref.call(null,new cljs.core.Keyword(null,"state","state",1123661827).cljs$core$IFn$_invoke$arity$1(game)));var u = cljs.core.nth.call(null,vec__4796,0,null);var r = cljs.core.nth.call(null,vec__4796,1,null);u.call(null,game);
return r.call(null,game);
}).call(null);
});
/**
* Runs the main loop of a game. If no fps
* is provided, will run at native fps.
*/
ezglib.core.main_loop = (function() {
var main_loop = null;
var main_loop__2 = (function (game,state_id){return ezglib.core.game_loop.call(null,game,state_id,requestAnimationFrame);
});
var main_loop__3 = (function (game,state_id,fps){return ezglib.core.game_loop.call(null,game,state_id,(function (cb){return setTimeout(cb,(1000 / fps));
}));
});
main_loop = function(game,state_id,fps){
switch(arguments.length){
case 2:
return main_loop__2.call(this,game,state_id);
case 3:
return main_loop__3.call(this,game,state_id,fps);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
main_loop.cljs$core$IFn$_invoke$arity$2 = main_loop__2;
main_loop.cljs$core$IFn$_invoke$arity$3 = main_loop__3;
return main_loop;
})()
;
/**
* Ends the main loop of the game.
*/
ezglib.core.end_game = (function end_game(game){return cljs.core.reset_BANG_.call(null,new cljs.core.Keyword(null,"loop","loop",1017231894).cljs$core$IFn$_invoke$arity$1(game),false);
});

//# sourceMappingURL=core.js.map