# Cinema Planning System

## Explanation

#### Model
- Each thing, that is happening in the cinema is strictly related with the room. 
It can be show, cleaning slot, some private conference/training, some e-sport tournament etc.
Each of those, are events, that are happening in a room. That's why I modeled base class RoomEvent and
deriving from it Show, CleaningSlot and Unavailability. We can think of it as different room states.
- Movie catalog is simplified to store movies with duration and 3D glasses indicator
- Room catalog is simple, as by cinema definition it shouldn't change a lot. There is a
name and cleaning slot duration (defaults to some value), which can be updated. 
(I decided to not implement the update flow further than the aggregate, it could be added in the future)

#### DB/Repository
All repositories are in memory for simplicity of the solution.
- At DB level, we'll have a table (room_events) which will store all the needed event information,
including type, time range etc. Table will also have domain specific columns like movie_id
or unavailability_reason, but thanks to application level modeling, it will be impossible to
create an entity in invalid state (e.g. cleaning slot with movie_id)
- I also decided to implement repository per each entity (which should only query for 
room_events of given type) as we might need some specific actions, along with a RoomEventRepository
which is aggregating all the results.
- There are also simple repositories for Movie and Room aggregates.

#### Command/Query
Command/Query handlers should be the called from the API Controller levels.
I was considering using some abstraction rather than calling specific handlers (e.g. Mediator pattern), 
but decided to not implement it at this point.
- To create a new roomEvent I used commands, which are calling appropriate 
repositories and factories to persist new events. 
- To view the cinema board plan, I used similar approach, but with query and its handler. 
- Query handler is calling the roomEventRepository for events happening in provided days, 
mapping it to the view model and returning

#### Validation
I decided to implement a single validator, which can be reused in factories of all the
roomEvent types. Depending on the needs, we can validate:
- room availability in given time
- event being within working hours
- event being within premiere hours

#### Tests
I decided to implement tests mainly on the command/query handlers level, which are checking
various cases happening in the flow, because IMHO it's more clean and faster approach.
Besides them, I added tests for checking the room availability, as it might be used in 
other scopes as well and it's crucial it works correctly.

#### Other
- We should consider the term of WorkingHours and if we should finish show before working hours, or show + cleaning slot
For this case purpose I decided to go with all events should happen within working hours