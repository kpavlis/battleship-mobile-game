package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.unipi.kpavlis.real_time_game_v2.Models.Opponent_Card;
import com.unipi.kpavlis.real_time_game_v2.R;

import java.util.List;

public class Opponents_Recycle_Adapter extends RecyclerView.Adapter<Opponents_Recycle_Adapter.OpponentViewHolder> {

    private List<Opponent_Card> opponentCards;
    private Context context_element;

    public Opponents_Recycle_Adapter(Context context, List<Opponent_Card> opponentCards) {
        this.context_element = context;
        this.opponentCards = opponentCards;
    }

    // This method is called to create a new view holder
    @NonNull
    @Override
    public OpponentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context_element).inflate(R.layout.opponent_card, viewGroup, false);

        return new OpponentViewHolder(view);
    }

    // This method is called to bind the data to the view holder elements
    @Override
    public void onBindViewHolder(OpponentViewHolder viewHolder, int position) {
        Opponent_Card current_opponent_card = opponentCards.get(position);

        viewHolder.fullName.setText(current_opponent_card.getFull_name());
        viewHolder.level.setText(String.valueOf(current_opponent_card.getLevel()));
        viewHolder.battle_Button.setTag(current_opponent_card.getOpponent_id());
        viewHolder.battle_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_opponent_card.getGameFragment().request_battle(view);
            }
        });
    }

    // This method returns the number of items in the list
    @Override
    public int getItemCount() {

        return opponentCards.size();

    }

    // This method is called to create a new view holder
    public static class OpponentViewHolder extends RecyclerView.ViewHolder {
        TextView fullName;
        TextView level;
        Button battle_Button;

        public OpponentViewHolder(View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.opponent_name_value);
            level = itemView.findViewById(R.id.opponent_level_value);
            battle_Button = itemView.findViewById(R.id.battle_button);
        }
    }

    // This method is called to update the list of opponent cards notifying the adapter
    public void updateOpponentCards(List<Opponent_Card> newOpponentCards) {
        this.opponentCards = newOpponentCards;
        notifyDataSetChanged();
    }
}
