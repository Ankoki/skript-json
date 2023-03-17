package com.btk5h.skriptjson.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import com.btk5h.skriptjson.SkriptJSON;
import com.btk5h.skriptjson.SkriptUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.Locale;

public class SecJSONToVariable extends Section {

  static {
    Skript.registerSection(SecJSONToVariable.class,
        "(map|copy) [the] json [(of|from)] %string% to [the] [var[iable]] %objects% async");
  }

  private Expression<String> json;
  private VariableString var;
  private boolean isLocal;
  private Trigger trigger;

  @Override
  public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
    this.trigger = loadCode(sectionNode, "async complete", ParserInstance.get().getCurrentEvents());
    this.json = (Expression<String>) exprs[0];
    Expression<?> expr = exprs[1];
    if (expr instanceof Variable) {
      Variable<?> varExpr = (Variable<?>) expr;
      if (varExpr.isList()) {
        var = SkriptUtil.getVariableName(varExpr);
        isLocal = varExpr.isLocal();
        return true;
      }
    }
    Skript.error(expr + " is not a list variable");
    return false;
  }

  @Override
  protected TriggerItem walk(Event event) {
    String json = this.json.getSingle(event);
    String var = this.var.toString(event).toLowerCase(Locale.ENGLISH);

    if (json == null) {
      return walk(event, false);
    }

      Bukkit.getScheduler().runTaskAsynchronously(SkriptJSON.getPlugin(SkriptJSON.class), () -> {
        try {
          Object parsed = new JSONParser().parse(json);
          EffJSONToVariable.mapFirst(event, var.substring(0, var.length() - 3), parsed, isLocal);
          trigger.execute(event);
        } catch (ParseException ex) {
          ex.printStackTrace();
        }
      });
    return walk(event, false);
  }

  @Override
  public String toString(Event e, boolean debug) {
    return "copy json from " + json.toString(e, debug) + " to " + var.toString(e, debug) + " async";
  }

}
