package me.sul.customentity.goal;


public interface NOTE {
}

// PathfinderGoal이 아닌 다른 클래스를 상속했을 때, a,b,c,d,e를 다 구현하면 부모 클래스의 이상한 변수들 신경 쓸 일 전혀 없음.
// ? 그럼 걍 PathfinderGoal 상속하면 되잖아 시발련아

// < Pathfind의 기본적인 흐름에 관한 메모 >
// 우선 canUse()가 계속 실행되면서 체크를 한다.
// canUse()가 true일 시 start()가 실행되고, start()가 실행되면 1틱마다 tick()가 실행된다.
// 그리고 canContinueToUse()는 tick()을 계속 실행해도 되는지 계속 체크한다.

// Navigation은 목적지를 찍으면 엔티티가 갈 수 있는 가장 근접한 위치로 목적지를 바꿔줌. -> y축 신경쓸거 없이 그냥 대충 목적지 찍으면 알아서 계산해줌