DROP PROCEDURE IF EXISTS `saveQuest`;
##
CREATE PROCEDURE `saveQuest` (p_playerid int, p_quest varchar(200), p_name varchar(45),
p_state varchar(45), p_statesCompleted varchar(256), p_active int, p_completed int)
BEGIN
    declare questId int;

    select id into questId
    from quests where playerid = p_playerid and path = p_quest;

    if questId is not null then
        update quests set name = p_name,
                          state = p_state,
                          statesCompleted = p_statesCompleted,
                          isActive = p_active,
                          isCompleted = p_completed
        where id = questId;
    else
        insert into quests (playerid, path, name, state, statesCompleted, isActive, isCompleted)
        values (p_playerid, p_quest, p_name, p_state, p_statesCompleted, p_active, p_completed);
    end if;
END;
##
